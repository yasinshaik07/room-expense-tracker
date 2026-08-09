package roomapp;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

import java.util.*;

@Controller
public class AppController {

    private final RoomRepo roomRepo;
    private final MemberRepo memberRepo;
    private final ExpenseRepo expenseRepo;
    private final AttendanceRepo attendanceRepo;
    private final SettlementRepo settlementRepo;

    public AppController(
            RoomRepo roomRepo,
            MemberRepo memberRepo,
            ExpenseRepo expenseRepo,
            AttendanceRepo attendanceRepo,
            SettlementRepo settlementRepo) {

        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.expenseRepo = expenseRepo;
        this.attendanceRepo = attendanceRepo;
        this.settlementRepo = settlementRepo;
    }

    // =====================================================
    // HOME
    // =====================================================

    @GetMapping("/")
    public String home(
            Model model,
            HttpSession session,

            @CookieValue(
                    value = "roomCode",
                    required = false
            )
            String savedRoomCode,

            @CookieValue(
                    value = "memberId",
                    required = false
            )
            String savedMemberId) {

        String roomCode =
                getRoomCode(session);

        // Remember Login
        if (roomCode == null) {

            restoreLoginFromCookies(
                    session,
                    savedRoomCode,
                    savedMemberId
            );

            roomCode =
                    getRoomCode(session);
        }

        // Not joined
        if (roomCode == null) {

            model.addAttribute(
                    "loggedIn",
                    false
            );

            return "home";
        }

        Room room =
                roomRepo
                        .findByRoomCode(roomCode)
                        .orElse(null);

        if (room == null) {

            session.invalidate();

            model.addAttribute(
                    "loggedIn",
                    false
            );

            return "home";
        }

        LocalDate today =
                LocalDate.now();

        YearMonth currentMonth =
                YearMonth.from(today);

        // ==========================
        // MEMBERS
        // ==========================

        List<Member> members =
                memberRepo
                        .findByRoomCodeOrderByNameAsc(
                                roomCode
                        );

        List<Member> activeMembers =
                memberRepo
                        .findByRoomCodeAndActiveTrueOrderByNameAsc(
                                roomCode
                        );

        // ==========================
        // ATTENDANCE
        // ==========================

        List<Attendance> todayAttendance =
                attendanceRepo
                        .findByRoomCodeAndAttendanceDate(
                                roomCode,
                                today
                        );

        Set<Long> presentIds =
                new HashSet<>();

        for (Attendance attendance :
                todayAttendance) {

            if (attendance.isPresent()) {

                presentIds.add(
                        attendance.getMemberId()
                );
            }
        }

        // ==========================
        // TODAY EXPENSES
        // ==========================

        List<Expense> todayExpenses =
                expenseRepo
                        .findByRoomCodeAndDeletedFalseAndExpenseDate(
                                roomCode,
                                today
                        );

        BigDecimal todayTotal =
                getTotal(todayExpenses);

        // ==========================
        // MONTH EXPENSES
        // ==========================

        List<Expense> monthExpenses =
                expenseRepo
                        .findByRoomCodeAndDeletedFalseAndExpenseDateBetween(
                                roomCode,
                                currentMonth.atDay(1),
                                currentMonth.atEndOfMonth()
                        );

        BigDecimal monthTotal =
                getTotal(monthExpenses);

        // ==========================
        // MONTH PAYMENTS
        // ==========================

        LocalDateTime monthStart =
                currentMonth
                        .atDay(1)
                        .atStartOfDay();

        LocalDateTime monthEnd =
                currentMonth
                        .atEndOfMonth()
                        .atTime(
                                LocalTime.MAX
                        );

        List<Settlement> monthPayments =
                settlementRepo
                        .findByRoomCodeAndPaidAtBetweenOrderByPaidAtDesc(
                                roomCode,
                                monthStart,
                                monthEnd
                        );

        // Full Payment History
        List<Settlement> settlementHistory =
                settlementRepo
                        .findByRoomCodeOrderByPaidAtDesc(
                                roomCode
                        );

        // ==========================
        // BALANCES
        // ==========================

        List<BalanceView> balances =
                calculateBalances(
                        members,
                        monthExpenses,
                        monthPayments
                );

        List<SettlementView> settlements =
                calculatePendingSettlements(
                        balances
                );

        // ==========================
        // HISTORY
        // ==========================

        List<Expense> history =
                expenseRepo
                        .findByRoomCodeAndDeletedFalseOrderByExpenseDateDescIdDesc(
                                roomCode
                        );

        List<Expense> deletedHistory =
                expenseRepo
                        .findByRoomCodeAndDeletedTrueOrderByDeletedAtDesc(
                                roomCode
                        );

        // ==========================
        // MODEL
        // ==========================

        model.addAttribute(
                "loggedIn",
                true
        );

        model.addAttribute(
                "room",
                room
        );

        model.addAttribute(
                "currentMemberName",
                session.getAttribute(
                        "memberName"
                )
        );

        model.addAttribute(
                "currentMemberId",
                session.getAttribute(
                        "memberId"
                )
        );

        model.addAttribute(
                "members",
                members
        );

        model.addAttribute(
                "activeMembers",
                activeMembers
        );

        model.addAttribute(
                "totalMembers",
                activeMembers.size()
        );

        model.addAttribute(
                "presentCount",
                presentIds.size()
        );

        model.addAttribute(
                "presentIds",
                presentIds
        );

        model.addAttribute(
                "today",
                today
        );

        model.addAttribute(
                "todayTotal",
                todayTotal
        );

        model.addAttribute(
                "monthTotal",
                monthTotal
        );

        model.addAttribute(
                "balances",
                balances
        );

        model.addAttribute(
                "settlements",
                settlements
        );

        model.addAttribute(
                "settlementHistory",
                settlementHistory
        );

        model.addAttribute(
                "expenses",
                history
        );

        model.addAttribute(
                "deletedExpenses",
                deletedHistory
        );

        return "home";
    }

    // =====================================================
    // CREATE ROOM
    // =====================================================

    @PostMapping("/room/create")
    public String createRoom(

            @RequestParam
            String roomName,

            @RequestParam
            String memberName,

            HttpSession session,

            HttpServletResponse response,

            RedirectAttributes redirect) {

        if (roomName == null ||
                roomName.trim().isEmpty() ||

                memberName == null ||
                memberName.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Room name and your name are required."
            );

            return "redirect:/";
        }

        String roomCode =
                generateRoomCode();

        Room room =
                new Room();

        room.setRoomName(
                roomName.trim()
        );

        room.setRoomCode(
                roomCode
        );

        // Database compatibility only
        room.setAdminName(
                "DISABLED"
        );

        room.setAdminPin(
                "DISABLED"
        );

        roomRepo.save(
                room
        );

        Member member =
                new Member();

        member.setName(
                memberName.trim()
        );

        member.setRoomCode(
                roomCode
        );

        member.setActive(true);

        member.setAdmin(false);

        member.setPresentToday(false);

        memberRepo.save(
                member
        );

        startSession(
                session,
                roomCode,
                member
        );

        saveLoginCookies(
                response,
                roomCode,
                member.getId()
        );

        redirect.addFlashAttribute(
                "success",
                "Room created. Room Code: "
                        + roomCode
        );

        return "redirect:/";
    }

    // =====================================================
    // JOIN ROOM
    // =====================================================

    @PostMapping("/room/join")
    public String joinRoom(

            @RequestParam
            String roomCode,

            @RequestParam
            String memberName,

            HttpSession session,

            HttpServletResponse response,

            RedirectAttributes redirect) {

        if (roomCode == null ||
                roomCode.trim().isEmpty() ||

                memberName == null ||
                memberName.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Room code and your name are required."
            );

            return "redirect:/";
        }

        String code =
                roomCode
                        .trim()
                        .toUpperCase();

        Room room =
                roomRepo
                        .findByRoomCode(code)
                        .orElse(null);

        if (room == null) {

            redirect.addFlashAttribute(
                    "error",
                    "Room code not found."
            );

            return "redirect:/";
        }

        List<Member> roomMembers =
                memberRepo
                        .findByRoomCodeOrderByNameAsc(
                                code
                        );

        Member currentMember =
                null;

        for (Member member :
                roomMembers) {

            if (member
                    .getName()
                    .equalsIgnoreCase(
                            memberName.trim()
                    )) {

                currentMember =
                        member;

                break;
            }
        }

        // New member
        if (currentMember == null) {

            currentMember =
                    new Member();

            currentMember.setName(
                    memberName.trim()
            );

            currentMember.setRoomCode(
                    code
            );

            currentMember.setActive(true);

            currentMember.setAdmin(false);

            currentMember.setPresentToday(false);

            memberRepo.save(
                    currentMember
            );

        } else {

            // Old member joining again
            if (!currentMember.isActive()) {

                currentMember.setActive(
                        true
                );

                memberRepo.save(
                        currentMember
                );
            }
        }

        startSession(
                session,
                code,
                currentMember
        );

        saveLoginCookies(
                response,
                code,
                currentMember.getId()
        );

        redirect.addFlashAttribute(
                "success",
                "Joined "
                        + room.getRoomName()
        );

        return "redirect:/";
    }

    // =====================================================
    // LEAVE ROOM
    // =====================================================

    @PostMapping("/room/leave")
    public String leaveRoom(

            HttpSession session,

            HttpServletResponse response) {

        session.invalidate();

        deleteCookie(
                response,
                "roomCode"
        );

        deleteCookie(
                response,
                "memberId"
        );

        return "redirect:/";
    }

    // =====================================================
    // ADD MEMBER
    // EVERYONE HAS SAME RIGHTS
    // =====================================================

    @PostMapping("/member/add")
    public String addMember(

            @RequestParam
            String name,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        if (name == null ||
                name.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Enter member name."
            );

            return "redirect:/";
        }

        List<Member> members =
                memberRepo
                        .findByRoomCodeOrderByNameAsc(
                                roomCode
                        );

        for (Member member :
                members) {

            if (member
                    .getName()
                    .equalsIgnoreCase(
                            name.trim()
                    )) {

                if (!member.isActive()) {

                    member.setActive(
                            true
                    );

                    memberRepo.save(
                            member
                    );

                    redirect.addFlashAttribute(
                            "success",
                            "Old member restored."
                    );

                } else {

                    redirect.addFlashAttribute(
                            "error",
                            "Member already exists."
                    );
                }

                return "redirect:/";
            }
        }

        Member member =
                new Member();

        member.setName(
                name.trim()
        );

        member.setRoomCode(
                roomCode
        );

        member.setActive(true);

        member.setAdmin(false);

        member.setPresentToday(false);

        memberRepo.save(
                member
        );

        redirect.addFlashAttribute(
                "success",
                "Member added."
        );

        return "redirect:/";
    }

    // =====================================================
    // REMOVE MEMBER
    // SOFT REMOVE ONLY
    // =====================================================

    @PostMapping("/member/remove/{id}")
    public String removeMember(

            @PathVariable
            Long id,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Member member =
                memberRepo
                        .findById(id)
                        .orElse(null);

        if (member == null ||
                !Objects.equals(
                        member.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Member not found in this room."
            );

            return "redirect:/";
        }

        Long currentMemberId =
                getCurrentMemberId(
                        session
                );

        if (Objects.equals(
                currentMemberId,
                member.getId()
        )) {

            redirect.addFlashAttribute(
                    "error",
                    "Use Leave Room if you want to leave."
            );

            return "redirect:/";
        }

        member.setActive(
                false
        );

        memberRepo.save(
                member
        );

        redirect.addFlashAttribute(
                "success",
                "Member removed. Old history is safe."
        );

        return "redirect:/";
    }

    // =====================================================
    // RESTORE MEMBER
    // =====================================================

    @PostMapping("/member/restore/{id}")
    public String restoreMember(

            @PathVariable
            Long id,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Member member =
                memberRepo
                        .findById(id)
                        .orElse(null);

        if (member == null ||
                !Objects.equals(
                        member.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Member not found in this room."
            );

            return "redirect:/";
        }

        member.setActive(
                true
        );

        memberRepo.save(
                member
        );

        redirect.addFlashAttribute(
                "success",
                "Member restored."
        );

        return "redirect:/";
    }

    // =====================================================
    // ATTENDANCE
    // =====================================================

    @Transactional
    @PostMapping("/attendance/save")
    public String saveAttendance(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate attendanceDate,

            @RequestParam(
                    required = false
            )
            List<Long> presentIds,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        attendanceRepo
                .deleteByRoomCodeAndAttendanceDate(
                        roomCode,
                        attendanceDate
                );

        List<Member> activeMembers =
                memberRepo
                        .findByRoomCodeAndActiveTrueOrderByNameAsc(
                                roomCode
                        );

        for (Member member :
                activeMembers) {

            Attendance attendance =
                    new Attendance();

            attendance.setMemberId(
                    member.getId()
            );

            attendance.setMemberName(
                    member.getName()
            );

            attendance.setRoomCode(
                    roomCode
            );

            attendance.setAttendanceDate(
                    attendanceDate
            );

            boolean present =
                    presentIds != null &&
                            presentIds.contains(
                                    member.getId()
                            );

            attendance.setPresent(
                    present
            );

            attendanceRepo.save(
                    attendance
            );
        }

        redirect.addFlashAttribute(
                "success",
                "Attendance saved."
        );

        return "redirect:/";
    }

    // =====================================================
    // ADD EXPENSE
    // =====================================================

    @PostMapping("/expense/add")
    public String addExpense(

            @RequestParam
            Long paidById,

            @RequestParam
            String itemName,

            @RequestParam
            BigDecimal amount,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate expenseDate,

            @RequestParam
            String splitMode,

            @RequestParam(
                    required = false
            )
            List<Long> selectedIds,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Member payer =
                memberRepo
                        .findById(paidById)
                        .orElse(null);

        if (payer == null ||

                !payer.isActive() ||

                !Objects.equals(
                        payer.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Invalid payer."
            );

            return "redirect:/";
        }

        if (itemName == null ||
                itemName.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Enter expense name."
            );

            return "redirect:/";
        }

        if (amount == null ||
                amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            redirect.addFlashAttribute(
                    "error",
                    "Enter correct amount."
            );

            return "redirect:/";
        }

        List<Member> shareMembers =
                getShareMembers(
                        roomCode,
                        splitMode,
                        expenseDate,
                        selectedIds
                );

        if (shareMembers.isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "No members selected for expense."
            );

            return "redirect:/";
        }

        Expense expense =
                new Expense();

        expense.setRoomCode(
                roomCode
        );

        expense.setPaidById(
                payer.getId()
        );

        expense.setPaidByName(
                payer.getName()
        );

        expense.setItemName(
                itemName.trim()
        );

        expense.setAmount(
                amount.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        expense.setExpenseDate(
                expenseDate
        );

        expense.setSplitMode(
                splitMode
        );

        String ids =
                shareMembers
                        .stream()
                        .map(
                                member ->
                                        String.valueOf(
                                                member.getId()
                                        )
                        )
                        .reduce(
                                (a, b) ->
                                        a + "," + b
                        )
                        .orElse("");

        expense.setSharedMemberIds(
                ids
        );

        expense.setDeleted(
                false
        );

        expenseRepo.save(
                expense
        );

        redirect.addFlashAttribute(
                "success",
                "Expense saved."
        );

        return "redirect:/";
    }

    // =====================================================
    // SOFT DELETE EXPENSE
    // EVERYONE CAN DO IT
    // =====================================================

    @PostMapping("/expense/delete/{id}")
    public String deleteExpense(

            @PathVariable
            Long id,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Expense expense =
                expenseRepo
                        .findById(id)
                        .orElse(null);

        if (expense == null ||
                !Objects.equals(
                        expense.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Expense not found in this room."
            );

            return "redirect:/";
        }

        expense.setDeleted(
                true
        );

        expense.setDeletedBy(
                String.valueOf(
                        session.getAttribute(
                                "memberName"
                        )
                )
        );

        expense.setDeletedAt(
                LocalDateTime.now()
        );

        expenseRepo.save(
                expense
        );

        redirect.addFlashAttribute(
                "success",
                "Expense moved to Deleted History."
        );

        return "redirect:/";
    }

    // =====================================================
    // RESTORE EXPENSE
    // =====================================================

    @PostMapping("/expense/restore/{id}")
    public String restoreExpense(

            @PathVariable
            Long id,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Expense expense =
                expenseRepo
                        .findById(id)
                        .orElse(null);

        if (expense == null ||
                !Objects.equals(
                        expense.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Expense not found in this room."
            );

            return "redirect:/";
        }

        expense.setDeleted(
                false
        );

        expense.setDeletedBy(
                null
        );

        expense.setDeletedAt(
                null
        );

        expenseRepo.save(
                expense
        );

        redirect.addFlashAttribute(
                "success",
                "Expense restored."
        );

        return "redirect:/";
    }

    // =====================================================
    // MARK SETTLEMENT PAID
    // EVERYONE CAN MARK
    // HISTORY SHOWS WHO MARKED IT
    // =====================================================

    @PostMapping("/settlement/paid")
    public String markSettlementPaid(

            @RequestParam
            Long fromMemberId,

            @RequestParam
            Long toMemberId,

            @RequestParam
            BigDecimal amount,

            HttpSession session,

            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {

            return "redirect:/";
        }

        Member fromMember =
                memberRepo
                        .findById(
                                fromMemberId
                        )
                        .orElse(null);

        Member toMember =
                memberRepo
                        .findById(
                                toMemberId
                        )
                        .orElse(null);

        if (fromMember == null ||
                toMember == null ||

                !Objects.equals(
                        fromMember.getRoomCode(),
                        roomCode
                ) ||

                !Objects.equals(
                        toMember.getRoomCode(),
                        roomCode
                )) {

            redirect.addFlashAttribute(
                    "error",
                    "Invalid room payment."
            );

            return "redirect:/";
        }

        if (amount == null ||
                amount.compareTo(
                        BigDecimal.ZERO
                ) <= 0) {

            redirect.addFlashAttribute(
                    "error",
                    "Invalid payment amount."
            );

            return "redirect:/";
        }

        Settlement settlement =
                new Settlement();

        settlement.setRoomCode(
                roomCode
        );

        settlement.setFromMemberId(
                fromMember.getId()
        );

        settlement.setFromName(
                fromMember.getName()
        );

        settlement.setToMemberId(
                toMember.getId()
        );

        settlement.setToName(
                toMember.getName()
        );

        settlement.setAmount(
                amount.setScale(
                        2,
                        RoundingMode.HALF_UP
                )
        );

        settlement.setPaidAt(
                LocalDateTime.now()
        );

        settlement.setRecordedBy(
                String.valueOf(
                        session.getAttribute(
                                "memberName"
                        )
                )
        );

        settlementRepo.save(
                settlement
        );

        redirect.addFlashAttribute(
                "success",
                fromMember.getName()
                        + " paid ₹"
                        + settlement.getAmount()
                        + " to "
                        + toMember.getName()
        );

        return "redirect:/";
    }

    // =====================================================
    // GET SHARE MEMBERS
    // =====================================================

    private List<Member> getShareMembers(

            String roomCode,

            String splitMode,

            LocalDate expenseDate,

            List<Long> selectedIds) {

        List<Member> activeMembers =
                memberRepo
                        .findByRoomCodeAndActiveTrueOrderByNameAsc(
                                roomCode
                        );

        // ALL
        if ("ALL".equals(
                splitMode
        )) {

            return activeMembers;
        }

        // SELECTED
        if ("SELECTED".equals(
                splitMode
        )) {

            List<Member> result =
                    new ArrayList<>();

            if (selectedIds == null) {

                return result;
            }

            for (Long id :
                    selectedIds) {

                memberRepo
                        .findById(id)
                        .ifPresent(
                                member -> {

                                    if (member.isActive() &&

                                            Objects.equals(
                                                    member.getRoomCode(),
                                                    roomCode
                                            )) {

                                        result.add(
                                                member
                                        );
                                    }
                                }
                        );
            }

            return result;
        }

        // PRESENT
        if ("PRESENT".equals(
                splitMode
        )) {

            List<Attendance> attendanceList =
                    attendanceRepo
                            .findByRoomCodeAndAttendanceDate(
                                    roomCode,
                                    expenseDate
                            );

            List<Member> result =
                    new ArrayList<>();

            for (Attendance attendance :
                    attendanceList) {

                if (!attendance.isPresent()) {

                    continue;
                }

                memberRepo
                        .findById(
                                attendance.getMemberId()
                        )
                        .ifPresent(
                                member -> {

                                    if (member.isActive() &&

                                            Objects.equals(
                                                    member.getRoomCode(),
                                                    roomCode
                                            )) {

                                        result.add(
                                                member
                                        );
                                    }
                                }
                        );
            }

            return result;
        }

        return new ArrayList<>();
    }

    // =====================================================
    // TOTAL
    // =====================================================

    private BigDecimal getTotal(

            List<Expense> expenses) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Expense expense :
                expenses) {

            total =
                    total.add(
                            expense.getAmount()
                    );
        }

        return total;
    }

    // =====================================================
    // PARSE SHARED MEMBER IDS
    // =====================================================

    private List<Long> parseIds(

            String text) {

        List<Long> ids =
                new ArrayList<>();

        if (text == null ||
                text.isBlank()) {

            return ids;
        }

        String[] values =
                text.split(",");

        for (String value :
                values) {

            try {

                ids.add(
                        Long.parseLong(
                                value.trim()
                        )
                );

            } catch (Exception ignored) {
            }
        }

        return ids;
    }

    // =====================================================
    // CALCULATE BALANCES
    // =====================================================

    private List<BalanceView> calculateBalances(

            List<Member> members,

            List<Expense> expenses,

            List<Settlement> payments) {

        Map<Long, BigDecimal> paidMap =
                new HashMap<>();

        Map<Long, BigDecimal> shareMap =
                new HashMap<>();

        // ==========================
        // EXPENSES
        // ==========================

        for (Expense expense :
                expenses) {

            Long payerId =
                    expense.getPaidById();

            paidMap.put(
                    payerId,

                    paidMap
                            .getOrDefault(
                                    payerId,
                                    BigDecimal.ZERO
                            )
                            .add(
                                    expense.getAmount()
                            )
            );

            List<Long> shareIds =
                    parseIds(
                            expense.getSharedMemberIds()
                    );

            if (shareIds.isEmpty()) {

                continue;
            }

            long totalPaise =
                    expense
                            .getAmount()
                            .movePointRight(2)
                            .longValue();

            long base =
                    totalPaise /
                            shareIds.size();

            long remainder =
                    totalPaise %
                            shareIds.size();

            for (int i = 0;
                 i < shareIds.size();
                 i++) {

                long paise =
                        base +
                                (
                                        i < remainder
                                                ? 1
                                                : 0
                                );

                BigDecimal memberShare =
                        BigDecimal
                                .valueOf(
                                        paise
                                )
                                .movePointLeft(
                                        2
                                );

                Long memberId =
                        shareIds.get(i);

                shareMap.put(
                        memberId,

                        shareMap
                                .getOrDefault(
                                        memberId,
                                        BigDecimal.ZERO
                                )
                                .add(
                                        memberShare
                                )
                );
            }
        }

        // ==========================
        // INITIAL BALANCE
        // ==========================

        Map<Long, BigDecimal> balanceMap =
                new HashMap<>();

        for (Member member :
                members) {

            BigDecimal paid =
                    paidMap.getOrDefault(
                            member.getId(),
                            BigDecimal.ZERO
                    );

            BigDecimal share =
                    shareMap.getOrDefault(
                            member.getId(),
                            BigDecimal.ZERO
                    );

            balanceMap.put(
                    member.getId(),
                    paid.subtract(
                            share
                    )
            );
        }

        // ==========================
        // APPLY PAID SETTLEMENTS
        // ==========================

        for (Settlement payment :
                payments) {

            Long fromId =
                    payment.getFromMemberId();

            Long toId =
                    payment.getToMemberId();

            BigDecimal amount =
                    payment.getAmount();

            // payer debt reduces
            balanceMap.put(
                    fromId,

                    balanceMap
                            .getOrDefault(
                                    fromId,
                                    BigDecimal.ZERO
                            )
                            .add(
                                    amount
                            )
            );

            // receiver amount reduces
            balanceMap.put(
                    toId,

                    balanceMap
                            .getOrDefault(
                                    toId,
                                    BigDecimal.ZERO
                            )
                            .subtract(
                                    amount
                            )
            );
        }

        // ==========================
        // FINAL RESULTS
        // ==========================

        List<BalanceView> result =
                new ArrayList<>();

        for (Member member :
                members) {

            BigDecimal paid =
                    paidMap.getOrDefault(
                            member.getId(),
                            BigDecimal.ZERO
                    );

            BigDecimal share =
                    shareMap.getOrDefault(
                            member.getId(),
                            BigDecimal.ZERO
                    );

            BigDecimal balance =
                    balanceMap.getOrDefault(
                            member.getId(),
                            BigDecimal.ZERO
                    );

            if (member.isActive() ||

                    paid.compareTo(
                            BigDecimal.ZERO
                    ) != 0 ||

                    share.compareTo(
                            BigDecimal.ZERO
                    ) != 0 ||

                    balance.compareTo(
                            BigDecimal.ZERO
                    ) != 0) {

                result.add(
                        new BalanceView(

                                member.getId(),

                                member.getName(),

                                paid,

                                share,

                                balance
                        )
                );
            }
        }

        return result;
    }

    // =====================================================
    // WHO PAYS WHOM
    // =====================================================

    private List<SettlementView>
    calculatePendingSettlements(

            List<BalanceView> balances) {

        List<BalanceTemp> payers =
                new ArrayList<>();

        List<BalanceTemp> receivers =
                new ArrayList<>();

        for (BalanceView balance :
                balances) {

            // PAY
            if (balance.balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                payers.add(
                        new BalanceTemp(

                                balance.memberId,

                                balance.name,

                                balance.balance.abs()
                        )
                );
            }

            // RECEIVE
            if (balance.balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                receivers.add(
                        new BalanceTemp(

                                balance.memberId,

                                balance.name,

                                balance.balance
                        )
                );
            }
        }

        List<SettlementView> result =
                new ArrayList<>();

        int payerIndex =
                0;

        int receiverIndex =
                0;

        while (payerIndex <
                payers.size() &&

                receiverIndex <
                        receivers.size()) {

            BalanceTemp payer =
                    payers.get(
                            payerIndex
                    );

            BalanceTemp receiver =
                    receivers.get(
                            receiverIndex
                    );

            BigDecimal amount =
                    payer.amount.min(
                            receiver.amount
                    );

            if (amount.compareTo(
                    BigDecimal.ZERO
            ) > 0) {

                result.add(
                        new SettlementView(

                                payer.memberId,

                                payer.name,

                                receiver.memberId,

                                receiver.name,

                                amount
                        )
                );
            }

            payer.amount =
                    payer.amount.subtract(
                            amount
                    );

            receiver.amount =
                    receiver.amount.subtract(
                            amount
                    );

            if (payer.amount
                    .compareTo(
                            BigDecimal.ZERO
                    ) == 0) {

                payerIndex++;
            }

            if (receiver.amount
                    .compareTo(
                            BigDecimal.ZERO
                    ) == 0) {

                receiverIndex++;
            }
        }

        return result;
    }

    // =====================================================
    // GENERATE ROOM CODE
    // =====================================================

    private String generateRoomCode() {

        String characters =
                "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        Random random =
                new Random();

        String code;

        do {

            StringBuilder builder =
                    new StringBuilder();

            for (int i = 0;
                 i < 6;
                 i++) {

                builder.append(

                        characters.charAt(

                                random.nextInt(
                                        characters.length()
                                )
                        )
                );
            }

            code =
                    builder.toString();

        } while (

                roomRepo
                        .existsByRoomCode(
                                code
                        )
        );

        return code;
    }

    // =====================================================
    // SESSION
    // =====================================================

    private String getRoomCode(

            HttpSession session) {

        return (String)
                session.getAttribute(
                        "roomCode"
                );
    }

    private Long getCurrentMemberId(

            HttpSession session) {

        Object memberId =
                session.getAttribute(
                        "memberId"
                );

        if (memberId instanceof Long) {

            return (Long) memberId;
        }

        return null;
    }

    private void startSession(

            HttpSession session,

            String roomCode,

            Member member) {

        session.setAttribute(
                "roomCode",
                roomCode
        );

        session.setAttribute(
                "memberId",
                member.getId()
        );

        session.setAttribute(
                "memberName",
                member.getName()
        );
    }

    // =====================================================
    // REMEMBER LOGIN
    // =====================================================

    private void restoreLoginFromCookies(

            HttpSession session,

            String savedRoomCode,

            String savedMemberId) {

        if (savedRoomCode == null ||
                savedRoomCode.isBlank() ||

                savedMemberId == null ||
                savedMemberId.isBlank()) {

            return;
        }

        try {

            Long memberId =
                    Long.parseLong(
                            savedMemberId
                    );

            Member member =
                    memberRepo
                            .findById(
                                    memberId
                            )
                            .orElse(null);

            if (member == null ||

                    !member.isActive() ||

                    !Objects.equals(
                            member.getRoomCode(),
                            savedRoomCode
                    )) {

                return;
            }

            if (roomRepo
                    .findByRoomCode(
                            savedRoomCode
                    )
                    .isEmpty()) {

                return;
            }

            startSession(
                    session,
                    savedRoomCode,
                    member
            );

        } catch (
                NumberFormatException ignored
        ) {
        }
    }

    private void saveLoginCookies(

            HttpServletResponse response,

            String roomCode,

            Long memberId) {

        Cookie roomCookie =
                new Cookie(
                        "roomCode",
                        roomCode
                );

        roomCookie.setMaxAge(
                60 * 60 * 24 * 30
        );

        roomCookie.setPath(
                "/"
        );

        roomCookie.setHttpOnly(
                true
        );

        roomCookie.setSecure(
                true
        );

        Cookie memberCookie =
                new Cookie(
                        "memberId",
                        String.valueOf(
                                memberId
                        )
                );

        memberCookie.setMaxAge(
                60 * 60 * 24 * 30
        );

        memberCookie.setPath(
                "/"
        );

        memberCookie.setHttpOnly(
                true
        );

        memberCookie.setSecure(
                true
        );

        response.addCookie(
                roomCookie
        );

        response.addCookie(
                memberCookie
        );
    }

    private void deleteCookie(

            HttpServletResponse response,

            String name) {

        Cookie cookie =
                new Cookie(
                        name,
                        ""
                );

        cookie.setMaxAge(
                0
        );

        cookie.setPath(
                "/"
        );

        cookie.setHttpOnly(
                true
        );

        cookie.setSecure(
                true
        );

        response.addCookie(
                cookie
        );
    }

    // =====================================================
    // BALANCE VIEW
    // =====================================================

    public static class BalanceView {

        private final Long memberId;

        private final String name;

        private final BigDecimal paid;

        private final BigDecimal share;

        private final BigDecimal balance;

        public BalanceView(

                Long memberId,

                String name,

                BigDecimal paid,

                BigDecimal share,

                BigDecimal balance) {

            this.memberId =
                    memberId;

            this.name =
                    name;

            this.paid =
                    paid;

            this.share =
                    share;

            this.balance =
                    balance;
        }

        public Long getMemberId() {

            return memberId;
        }

        public String getName() {

            return name;
        }

        public BigDecimal getPaid() {

            return paid;
        }

        public BigDecimal getShare() {

            return share;
        }

        public BigDecimal getAmount() {

            return balance.abs();
        }

        public String getStatus() {

            if (balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                return "GET";
            }

            if (balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                return "PAY";
            }

            return "SETTLED";
        }
    }

    // =====================================================
    // SETTLEMENT VIEW
    // =====================================================

    public static class SettlementView {

        private final Long fromMemberId;

        private final String fromName;

        private final Long toMemberId;

        private final String toName;

        private final BigDecimal amount;

        public SettlementView(

                Long fromMemberId,

                String fromName,

                Long toMemberId,

                String toName,

                BigDecimal amount) {

            this.fromMemberId =
                    fromMemberId;

            this.fromName =
                    fromName;

            this.toMemberId =
                    toMemberId;

            this.toName =
                    toName;

            this.amount =
                    amount;
        }

        public Long getFromMemberId() {

            return fromMemberId;
        }

        public String getFromName() {

            return fromName;
        }

        public Long getToMemberId() {

            return toMemberId;
        }

        public String getToName() {

            return toName;
        }

        public BigDecimal getAmount() {

            return amount;
        }
    }

    // =====================================================
    // INTERNAL BALANCE TEMP
    // =====================================================

    private static class BalanceTemp {

        private final Long memberId;

        private final String name;

        private BigDecimal amount;

        public BalanceTemp(

                Long memberId,

                String name,

                BigDecimal amount) {

            this.memberId =
                    memberId;

            this.name =
                    name;

            this.amount =
                    amount;
        }
    }
}