// Shows a confirm dialog for any form, button or link that has a data-confirm attribute.
// Replaces inline onclick/onsubmit confirm handlers, which the CSP blocks.

document.addEventListener("submit", function (e) {
    var form = e.target;
    var message = form.getAttribute("data-confirm");
    if (message && !confirm(message)) {
        e.preventDefault();
    }
});

document.addEventListener("click", function (e) {
    var element = e.target.closest("[data-confirm]");
    if (element && element.tagName !== "FORM") {
        if (!confirm(element.getAttribute("data-confirm"))) {
            e.preventDefault();
        }
    }
});