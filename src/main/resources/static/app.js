function openPage(pageId, button) {

    const pages =
        document.querySelectorAll(".page");

    pages.forEach(function (page) {
        page.classList.remove("active-page");
    });


    const buttons =
        document.querySelectorAll(".nav-btn");

    buttons.forEach(function (btn) {
        btn.classList.remove("active");
    });


    const selectedPage =
        document.getElementById(pageId);

    if (selectedPage) {
        selectedPage.classList.add("active-page");
    }


    if (button) {
        button.classList.add("active");
    }


    const sidebar =
        document.querySelector(".sidebar");

    if (
        window.innerWidth <= 760 &&
        sidebar
    ) {
        sidebar.classList.remove("show");
    }


    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });


    // Remember current tab in browser
    localStorage.setItem(
        "roomAppPage",
        pageId
    );
}


function openPageByName(pageId) {

    const buttons =
        document.querySelectorAll(".nav-btn");

    for (const button of buttons) {

        const clickText =
            button.getAttribute("onclick");

        if (
            clickText &&
            clickText.includes("'" + pageId + "'")
        ) {

            openPage(
                pageId,
                button
            );

            return;
        }
    }


    const page =
        document.getElementById(pageId);

    if (page) {

        document
            .querySelectorAll(".page")
            .forEach(function (item) {

                item.classList.remove(
                    "active-page"
                );
            });


        page.classList.add(
            "active-page"
        );
    }
}


function toggleMenu() {

    const sidebar =
        document.querySelector(".sidebar");

    if (sidebar) {
        sidebar.classList.toggle("show");
    }
}


function showSelected() {

    const splitMode =
        document.getElementById("splitMode");

    const selectedBox =
        document.getElementById("selectedBox");

    if (
        !splitMode ||
        !selectedBox
    ) {
        return;
    }


    if (
        splitMode.value === "SELECTED"
    ) {

        selectedBox.style.display =
            "block";

    } else {

        selectedBox.style.display =
            "none";
    }
}


function restoreLastPage() {

    const savedPage =
        localStorage.getItem(
            "roomAppPage"
        );

    if (!savedPage) {
        return;
    }


    const page =
        document.getElementById(
            savedPage
        );

    if (!page) {
        return;
    }


    const buttons =
        document.querySelectorAll(
            ".nav-btn"
        );


    for (const button of buttons) {

        const clickText =
            button.getAttribute(
                "onclick"
            );

        if (
            clickText &&
            clickText.includes(
                "'" + savedPage + "'"
            )
        ) {

            openPage(
                savedPage,
                button
            );

            return;
        }
    }
}


function closeMenuWhenClickedOutside(
    event
) {

    if (window.innerWidth > 760) {
        return;
    }


    const sidebar =
        document.querySelector(
            ".sidebar"
        );

    const menuButton =
        document.querySelector(
            ".menu-btn"
        );


    if (
        !sidebar ||
        !sidebar.classList.contains(
            "show"
        )
    ) {
        return;
    }


    if (
        sidebar.contains(
            event.target
        )
    ) {
        return;
    }


    if (
        menuButton &&
        menuButton.contains(
            event.target
        )
    ) {
        return;
    }


    sidebar.classList.remove(
        "show"
    );
}


document.addEventListener(
    "DOMContentLoaded",
    function () {

        showSelected();

        restoreLastPage();


        document.addEventListener(
            "click",
            closeMenuWhenClickedOutside
        );


        const splitMode =
            document.getElementById(
                "splitMode"
            );

        if (splitMode) {

            splitMode.addEventListener(
                "change",
                showSelected
            );
        }
    }
);