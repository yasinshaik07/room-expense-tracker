package roomapp;

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
import java.time.YearMonth;
import java.util.*;

@Controller
public class AppController {

    private final RoomRepo roomRepo;
    private final MemberRepo memberRepo;
    private final ExpenseRepo expenseRepo;
    private final AttendanceRepo attendanceRepo;

    public AppController(
            RoomRepo roomRepo,
            MemberRepo memberRepo,
            ExpenseRepo expenseRepo,
            AttendanceRepo attendanceRepo) {

        this.roomRepo = roomRepo;
        this.memberRepo = memberRepo;
        this.expenseRepo = expenseRepo;
        this.attendanceRepo = attendanceRepo;
    }

    // =========================
    // HOME
    // =========================

    @GetMapping("/")
    public String home(
            Model model,
            HttpSession session) {

        String roomCode =
                (String) session.getAttribute("roomCode");

        if (roomCode == null) {

            model.addAttribute("loggedIn", false);

            return "home";
        }

        Room room =
                roomRepo.findByRoomCode(roomCode)
                        .orElse(null);

        if (room == null) {

            session.invalidate();

            model.addAttribute("loggedIn", false);

            return "home";
        }

        model.addAttribute("loggedIn", true);
        model.addAttribute("room", room);

        model.addAttribute(
                "currentMemberName",
                session.getAttribute("memberName")
        );

        boolean isAdmin =
                Boolean.TRUE.equals(
                        session.getAttribute("isAdmin")
                );

        model.addAttribute(
                "isAdmin",
                isAdmin
        );

        LocalDate today =
                LocalDate.now();

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

        List<Expense> todayExpenses =
                expenseRepo
                        .findByRoomCodeAndDeletedFalseAndExpenseDate(
                                roomCode,
                                today
                        );

        BigDecimal todayTotal =
                getTotal(todayExpenses);

        YearMonth month =
                YearMonth.from(today);

        List<Expense> monthExpenses =
                expenseRepo
                        .findByRoomCodeAndDeletedFalseAndExpenseDateBetween(
                                roomCode,
                                month.atDay(1),
                                month.atEndOfMonth()
                        );

        BigDecimal monthTotal =
                getTotal(monthExpenses);

        List<BalanceView> balances =
                calculateBalances(
                        members,
                        monthExpenses
                );

        List<SettlementView> settlements =
                calculateSettlements(
                        members,
                        monthExpenses
                );

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
                "expenses",
                history
        );

        model.addAttribute(
                "deletedExpenses",
                deletedHistory
        );

        return "home";
    }


    // =========================
    // CREATE ROOM
    // =========================

    @PostMapping("/room/create")
    public String createRoom(

            @RequestParam String roomName,
            @RequestParam String adminName,
            @RequestParam String adminPin,

            HttpSession session,
            RedirectAttributes redirect) {

        if (roomName == null ||
                roomName.trim().isEmpty() ||

                adminName == null ||
                adminName.trim().isEmpty() ||

                adminPin == null ||
                adminPin.trim().length() < 4) {

            redirect.addFlashAttribute(
                    "error",
                    "Enter room name, admin name and minimum 4 digit PIN."
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

        room.setAdminName(
                adminName.trim()
        );

        room.setAdminPin(
                adminPin.trim()
        );

        roomRepo.save(room);


        Member admin =
                new Member();

        admin.setName(
                adminName.trim()
        );

        admin.setRoomCode(
                roomCode
        );

        admin.setActive(true);
        admin.setAdmin(true);

        memberRepo.save(admin);


        session.setAttribute(
                "roomCode",
                roomCode
        );

        session.setAttribute(
                "memberName",
                admin.getName()
        );

        session.setAttribute(
                "memberId",
                admin.getId()
        );

        session.setAttribute(
                "isAdmin",
                true
        );

        redirect.addFlashAttribute(
                "success",
                "Room created. Room Code: " + roomCode
        );

        return "redirect:/";
    }


    // =========================
    // JOIN ROOM
    // =========================

    @PostMapping("/room/join")
    public String joinRoom(

            @RequestParam String roomCode,
            @RequestParam String memberName,

            HttpSession session,
            RedirectAttributes redirect) {

        if (roomCode == null ||
                roomCode.trim().isEmpty() ||

                memberName == null ||
                memberName.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Enter room code and your name."
            );

            return "redirect:/";
        }

        String code =
                roomCode.trim()
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

            if (member.getName()
                    .equalsIgnoreCase(
                            memberName.trim()
                    )) {

                currentMember =
                        member;

                break;
            }
        }


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

            memberRepo.save(
                    currentMember
            );

        } else {

            if (!currentMember.isActive()) {

                currentMember.setActive(true);

                memberRepo.save(
                        currentMember
                );
            }
        }


        session.setAttribute(
                "roomCode",
                code
        );

        session.setAttribute(
                "memberName",
                currentMember.getName()
        );

        session.setAttribute(
                "memberId",
                currentMember.getId()
        );

        session.setAttribute(
                "isAdmin",
                currentMember.isAdmin()
        );

        redirect.addFlashAttribute(
                "success",
                "Joined " + room.getRoomName()
        );

        return "redirect:/";
    }


    // =========================
    // ADMIN LOGIN
    // =========================

    @PostMapping("/admin/login")
    public String adminLogin(

            @RequestParam String adminPin,

            HttpSession session,
            RedirectAttributes redirect) {

        String roomCode =
                getRoomCode(session);

        if (roomCode == null) {
            return "redirect:/";
        }

        Room room =
                roomRepo
                        .findByRoomCode(roomCode)
                        .orElse(null);

        if (room == null) {
            return "redirect:/";
        }

        if (!room.getAdminPin()
                .equals(adminPin)) {

            redirect.addFlashAttribute(
                    "error",
                    "Wrong admin PIN."
            );

            return "redirect:/";
        }

        session.setAttribute(
                "isAdmin",
                true
        );

        redirect.addFlashAttribute(
                "success",
                "Admin access enabled."
        );

        return "redirect:/";
    }


    // =========================
    // LEAVE ROOM
    // =========================

    @PostMapping("/room/leave")
    public String leaveRoom(
            HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }


    // =========================
    // ADD MEMBER
    // ADMIN ONLY
    // =========================

    @PostMapping("/member/add")
    public String addMember(

            @RequestParam String name,

            HttpSession session,
            RedirectAttributes redirect) {

        if (!isAdmin(session)) {

            redirect.addFlashAttribute(
                    "error",
                    "Only admin can add members."
            );

            return "redirect:/";
        }

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

            if (member.getName()
                    .equalsIgnoreCase(
                            name.trim()
                    )) {

                if (!member.isActive()) {

                    member.setActive(true);

                    memberRepo.save(member);

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

        memberRepo.save(member);

        redirect.addFlashAttribute(
                "success",
                "Member added."
        );

        return "redirect:/";
    }


    // =========================
    // REMOVE MEMBER FROM ROOM
    // SOFT REMOVE
    // =========================

    @PostMapping("/member/remove/{id}")
    public String removeMember(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirect) {

        if (!isAdmin(session)) {

            redirect.addFlashAttribute(
                    "error",
                    "Only admin can remove members."
            );

            return "redirect:/";
        }

        String roomCode =
                getRoomCode(session);

        Member member =
                memberRepo
                        .findById(id)
                        .orElse(null);

        if (member == null ||
                !Objects.equals(
                        member.getRoomCode(),
                        roomCode
                )) {

            return "redirect:/";
        }

        if (member.isAdmin()) {

            redirect.addFlashAttribute(
                    "error",
                    "Room admin cannot be removed."
            );

            return "redirect:/";
        }

        member.setActive(false);

        memberRepo.save(member);

        redirect.addFlashAttribute(
                "success",
                "Member removed from active room list. History is safe."
        );

        return "redirect:/";
    }


    // =========================
    // RESTORE MEMBER
    // =========================

    @PostMapping("/member/restore/{id}")
    public String restoreMember(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirect) {

        if (!isAdmin(session)) {

            redirect.addFlashAttribute(
                    "error",
                    "Only admin can restore members."
            );

            return "redirect:/";
        }

        String roomCode =
                getRoomCode(session);

        Member member =
                memberRepo
                        .findById(id)
                        .orElse(null);

        if (member != null &&
                Objects.equals(
                        member.getRoomCode(),
                        roomCode
                )) {

            member.setActive(true);

            memberRepo.save(member);
        }

        return "redirect:/";
    }


    // =========================
    // ATTENDANCE
    // =========================

    @Transactional
    @PostMapping("/attendance/save")
    public String saveAttendance(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE)
            LocalDate attendanceDate,

            @RequestParam(required = false)
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


    // =========================
    // ADD EXPENSE
    // =========================

    @PostMapping("/expense/add")
    public String addExpense(

            @RequestParam Long paidById,

            @RequestParam String itemName,

            @RequestParam BigDecimal amount,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE)
            LocalDate expenseDate,

            @RequestParam String splitMode,

            @RequestParam(required = false)
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
                        BigDecimal.ZERO) <= 0) {

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
                shareMembers.stream()
                        .map(member ->
                                String.valueOf(
                                        member.getId()
                                ))
                        .reduce(
                                (a, b) ->
                                        a + "," + b
                        )
                        .orElse("");

        expense.setSharedMemberIds(
                ids
        );

        expense.setDeleted(false);

        expenseRepo.save(expense);


        redirect.addFlashAttribute(
                "success",
                "Expense saved."
        );

        return "redirect:/";
    }


    // =========================
    // SOFT DELETE EXPENSE
    // ADMIN ONLY
    // =========================

    @PostMapping("/expense/delete/{id}")
    public String deleteExpense(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirect) {

        if (!isAdmin(session)) {

            redirect.addFlashAttribute(
                    "error",
                    "Only admin can delete history."
            );

            return "redirect:/";
        }

        String roomCode =
                getRoomCode(session);

        Expense expense =
                expenseRepo
                        .findById(id)
                        .orElse(null);

        if (expense == null ||
                !Objects.equals(
                        expense.getRoomCode(),
                        roomCode
                )) {

            return "redirect:/";
        }

        expense.setDeleted(true);

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

        expenseRepo.save(expense);

        redirect.addFlashAttribute(
                "success",
                "Expense moved to Deleted History. It is not permanently erased."
        );

        return "redirect:/";
    }


    // =========================
    // RESTORE DELETED EXPENSE
    // ADMIN ONLY
    // =========================

    @PostMapping("/expense/restore/{id}")
    public String restoreExpense(

            @PathVariable Long id,

            HttpSession session,
            RedirectAttributes redirect) {

        if (!isAdmin(session)) {

            redirect.addFlashAttribute(
                    "error",
                    "Only admin can restore expenses."
            );

            return "redirect:/";
        }

        String roomCode =
                getRoomCode(session);

        Expense expense =
                expenseRepo
                        .findById(id)
                        .orElse(null);

        if (expense != null &&
                Objects.equals(
                        expense.getRoomCode(),
                        roomCode
                )) {

            expense.setDeleted(false);
            expense.setDeletedBy(null);
            expense.setDeletedAt(null);

            expenseRepo.save(expense);
        }

        return "redirect:/";
    }


    // =========================
    // SHARE MEMBERS
    // =========================

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

        if ("ALL".equals(splitMode)) {

            return activeMembers;
        }


        if ("SELECTED".equals(splitMode)) {

            if (selectedIds == null ||
                    selectedIds.isEmpty()) {

                return new ArrayList<>();
            }

            List<Member> result =
                    new ArrayList<>();

            for (Long id :
                    selectedIds) {

                memberRepo
                        .findById(id)
                        .ifPresent(member -> {

                            if (Objects.equals(
                                    member.getRoomCode(),
                                    roomCode
                            ) &&
                                    member.isActive()) {

                                result.add(member);
                            }
                        });
            }

            return result;
        }


        if ("PRESENT".equals(splitMode)) {

            List<Attendance> attendance =
                    attendanceRepo
                            .findByRoomCodeAndAttendanceDate(
                                    roomCode,
                                    expenseDate
                            );

            List<Long> ids =
                    new ArrayList<>();

            for (Attendance item :
                    attendance) {

                if (item.isPresent()) {

                    ids.add(
                            item.getMemberId()
                    );
                }
            }

            List<Member> result =
                    new ArrayList<>();

            for (Long id :
                    ids) {

                memberRepo
                        .findById(id)
                        .ifPresent(member -> {

                            if (Objects.equals(
                                    member.getRoomCode(),
                                    roomCode
                            )) {

                                result.add(member);
                            }
                        });
            }

            return result;
        }

        return new ArrayList<>();
    }


    // =========================
    // TOTAL
    // =========================

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


    // =========================
    // PARSE IDS
    // =========================

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


    // =========================
    // BALANCE
    // =========================

    private List<BalanceView> calculateBalances(

            List<Member> members,
            List<Expense> expenses) {

        Map<Long, BigDecimal> paidMap =
                new HashMap<>();

        Map<Long, BigDecimal> shareMap =
                new HashMap<>();


        for (Expense expense :
                expenses) {

            Long payerId =
                    expense.getPaidById();

            paidMap.put(
                    payerId,

                    paidMap.getOrDefault(
                            payerId,
                            BigDecimal.ZERO
                    ).add(
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
                                (i < remainder
                                        ? 1
                                        : 0);

                BigDecimal share =
                        BigDecimal
                                .valueOf(paise)
                                .movePointLeft(2);

                Long memberId =
                        shareIds.get(i);

                shareMap.put(
                        memberId,

                        shareMap.getOrDefault(
                                memberId,
                                BigDecimal.ZERO
                        ).add(share)
                );
            }
        }


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
                    paid.subtract(share);


            if (member.isActive() ||
                    paid.compareTo(
                            BigDecimal.ZERO) != 0 ||
                    share.compareTo(
                            BigDecimal.ZERO) != 0) {

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


    // =========================
    // EXACT SETTLEMENT
    // =========================

    private List<SettlementView> calculateSettlements(

            List<Member> members,
            List<Expense> expenses) {

        List<BalanceView> balances =
                calculateBalances(
                        members,
                        expenses
                );


        List<BalanceTemp> payers =
                new ArrayList<>();

        List<BalanceTemp> receivers =
                new ArrayList<>();


        for (BalanceView balance :
                balances) {

            if (balance.balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) < 0) {

                payers.add(
                        new BalanceTemp(
                                balance.name,
                                balance.balance
                                        .abs()
                )
                );
            }

            if (balance.balance
                    .compareTo(
                            BigDecimal.ZERO
                    ) > 0) {

                receivers.add(
                        new BalanceTemp(
                                balance.name,
                                balance.balance
                )
                );
            }
        }


        List<SettlementView> result =
                new ArrayList<>();

        int i = 0;
        int j = 0;


        while (i < payers.size() &&
                j < receivers.size()) {

            BalanceTemp payer =
                    payers.get(i);

            BalanceTemp receiver =
                    receivers.get(j);

            BigDecimal amount =
                    payer.amount.min(
                            receiver.amount
                    );

            if (amount.compareTo(
                    BigDecimal.ZERO) > 0) {

                result.add(
                        new SettlementView(
                                payer.name,
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

                i++;
            }

            if (receiver.amount
                    .compareTo(
                            BigDecimal.ZERO
                    ) == 0) {

                j++;
            }
        }

        return result;
    }


    // =========================
    // ROOM CODE
    // =========================

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
                roomRepo.existsByRoomCode(
                        code
                )
        );

        return code;
    }


    private String getRoomCode(
            HttpSession session) {

        return (String)
                session.getAttribute(
                        "roomCode"
                );
    }


    private boolean isAdmin(
            HttpSession session) {

        return Boolean.TRUE.equals(
                session.getAttribute(
                        "isAdmin"
                )
        );
    }


    // =========================
    // VIEW CLASSES
    // =========================

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

            if (balance.compareTo(
                    BigDecimal.ZERO) > 0) {

                return "GET";
            }

            if (balance.compareTo(
                    BigDecimal.ZERO) < 0) {

                return "PAY";
            }

            return "SETTLED";
        }
    }


    public static class SettlementView {

        private final String fromName;
        private final String toName;
        private final BigDecimal amount;

        public SettlementView(
                String fromName,
                String toName,
                BigDecimal amount) {

            this.fromName =
                    fromName;

            this.toName =
                    toName;

            this.amount =
                    amount;
        }

        public String getFromName() {
            return fromName;
        }

        public String getToName() {
            return toName;
        }

        public BigDecimal getAmount() {
            return amount;
        }
    }


    private static class BalanceTemp {

        private final String name;
        private BigDecimal amount;

        public BalanceTemp(
                String name,
                BigDecimal amount) {

            this.name =
                    name;

            this.amount =
                    amount;
        }
    }
}