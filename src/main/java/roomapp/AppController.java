package roomapp;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
public class AppController {

    private final MemberRepo memberRepo;
    private final ExpenseRepo expenseRepo;
    private final AttendanceRepo attendanceRepo;

    public AppController(
            MemberRepo memberRepo,
            ExpenseRepo expenseRepo,
            AttendanceRepo attendanceRepo) {

        this.memberRepo = memberRepo;
        this.expenseRepo = expenseRepo;
        this.attendanceRepo = attendanceRepo;
    }

    @GetMapping("/")
    public String home(Model model) {

        LocalDate today = LocalDate.now();

        List<Member> members = memberRepo.findAll();

        List<Member> activeMembers =
                memberRepo.findByActiveTrueOrderByNameAsc();

        List<Attendance> todayAttendance =
                attendanceRepo.findByAttendanceDate(today);

        Set<Long> presentIds = new HashSet<>();

        for (Attendance attendance : todayAttendance) {

            if (attendance.isPresent()) {
                presentIds.add(attendance.getMemberId());
            }
        }

        List<Expense> todayExpenses =
                expenseRepo.findByExpenseDate(today);

        BigDecimal todayTotal =
                getTotal(todayExpenses);

        YearMonth month =
                YearMonth.from(today);

        List<Expense> monthExpenses =
                expenseRepo.findByExpenseDateBetween(
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

        model.addAttribute("members", members);
        model.addAttribute("activeMembers", activeMembers);

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

        model.addAttribute("today", today);
        model.addAttribute("todayTotal", todayTotal);
        model.addAttribute("monthTotal", monthTotal);
        model.addAttribute("balances", balances);

        model.addAttribute(
                "expenses",
                expenseRepo.findAllByOrderByExpenseDateDescIdDesc()
        );

        return "home";
    }


    // ADD MEMBER

    @PostMapping("/member/add")
    public String addMember(
            @RequestParam String name,
            RedirectAttributes redirect) {

        if (name == null || name.trim().isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Please enter member name."
            );

            return "redirect:/";
        }

        Member member = new Member();

        member.setName(name.trim());
        member.setActive(true);

        memberRepo.save(member);

        redirect.addFlashAttribute(
                "success",
                "Member added successfully."
        );

        return "redirect:/";
    }


    // ACTIVE / INACTIVE

    @PostMapping("/member/toggle/{id}")
    public String toggleMember(
            @PathVariable Long id) {

        Member member =
                memberRepo.findById(id).orElse(null);

        if (member != null) {

            member.setActive(
                    !member.isActive()
            );

            memberRepo.save(member);
        }

        return "redirect:/";
    }


    // ATTENDANCE

    @Transactional
    @PostMapping("/attendance/save")
    public String saveAttendance(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE)
            LocalDate attendanceDate,

            @RequestParam(required = false)
            List<Long> presentIds,

            RedirectAttributes redirect) {

        attendanceRepo
                .deleteByAttendanceDate(
                        attendanceDate
                );

        List<Member> activeMembers =
                memberRepo
                        .findByActiveTrueOrderByNameAsc();

        for (Member member : activeMembers) {

            Attendance attendance =
                    new Attendance();

            attendance.setMemberId(
                    member.getId()
            );

            attendance.setMemberName(
                    member.getName()
            );

            attendance.setAttendanceDate(
                    attendanceDate
            );

            boolean present =
                    presentIds != null &&
                    presentIds.contains(
                            member.getId()
                    );

            attendance.setPresent(present);

            attendanceRepo.save(attendance);
        }

        redirect.addFlashAttribute(
                "success",
                "Attendance saved successfully."
        );

        return "redirect:/";
    }


    // ADD EXPENSE

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

            RedirectAttributes redirect) {

        Member payer =
                memberRepo
                        .findById(paidById)
                        .orElse(null);

        if (payer == null) {

            redirect.addFlashAttribute(
                    "error",
                    "Select who paid."
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
                        splitMode,
                        expenseDate,
                        selectedIds
                );

        if (shareMembers.isEmpty()) {

            redirect.addFlashAttribute(
                    "error",
                    "Select members for this expense."
            );

            return "redirect:/";
        }

        Expense expense = new Expense();

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
                                (a, b) -> a + "," + b
                        )
                        .orElse("");

        expense.setSharedMemberIds(ids);

        expenseRepo.save(expense);

        redirect.addFlashAttribute(
                "success",
                "Expense added successfully."
        );

        return "redirect:/";
    }


    // DELETE EXPENSE

    @PostMapping("/expense/delete/{id}")
    public String deleteExpense(
            @PathVariable Long id) {

        if (expenseRepo.existsById(id)) {
            expenseRepo.deleteById(id);
        }

        return "redirect:/";
    }


    // GET SHARE MEMBERS

    private List<Member> getShareMembers(
            String splitMode,
            LocalDate expenseDate,
            List<Long> selectedIds) {

        if ("ALL".equals(splitMode)) {

            return memberRepo
                    .findByActiveTrueOrderByNameAsc();
        }

        if ("SELECTED".equals(splitMode)) {

            if (selectedIds == null ||
                    selectedIds.isEmpty()) {

                return new ArrayList<>();
            }

            return memberRepo
                    .findAllById(selectedIds);
        }

        if ("PRESENT".equals(splitMode)) {

            List<Attendance> attendanceList =
                    attendanceRepo
                            .findByAttendanceDate(
                                    expenseDate
                            );

            List<Long> ids =
                    new ArrayList<>();

            for (Attendance attendance :
                    attendanceList) {

                if (attendance.isPresent()) {

                    ids.add(
                            attendance.getMemberId()
                    );
                }
            }

            if (ids.isEmpty()) {
                return new ArrayList<>();
            }

            return memberRepo
                    .findAllById(ids);
        }

        return new ArrayList<>();
    }


    // TOTAL

    private BigDecimal getTotal(
            List<Expense> expenses) {

        BigDecimal total =
                BigDecimal.ZERO;

        for (Expense expense : expenses) {

            total = total.add(
                    expense.getAmount()
            );
        }

        return total;
    }


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

        for (String value : values) {

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


    // BALANCE

    private List<BalanceView> calculateBalances(
            List<Member> members,
            List<Expense> expenses) {

        Map<Long, BigDecimal> paidMap =
                new HashMap<>();

        Map<Long, BigDecimal> shareMap =
                new HashMap<>();

        for (Expense expense : expenses) {

            Long payerId =
                    expense.getPaidById();

            BigDecimal oldPaid =
                    paidMap.getOrDefault(
                            payerId,
                            BigDecimal.ZERO
                    );

            paidMap.put(
                    payerId,
                    oldPaid.add(
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
                        (i < remainder ? 1 : 0);

                BigDecimal memberShare =
                        BigDecimal
                                .valueOf(paise)
                                .movePointLeft(2);

                Long memberId =
                        shareIds.get(i);

                BigDecimal oldShare =
                        shareMap.getOrDefault(
                                memberId,
                                BigDecimal.ZERO
                        );

                shareMap.put(
                        memberId,
                        oldShare.add(
                                memberShare
                        )
                );
            }
        }

        List<BalanceView> result =
                new ArrayList<>();

        for (Member member : members) {

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


    public static class BalanceView {

        private final String name;
        private final BigDecimal paid;
        private final BigDecimal share;
        private final BigDecimal balance;

        public BalanceView(
                String name,
                BigDecimal paid,
                BigDecimal share,
                BigDecimal balance) {

            this.name = name;
            this.paid = paid;
            this.share = share;
            this.balance = balance;
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
}