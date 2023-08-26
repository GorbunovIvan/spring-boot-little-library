document.addEventListener("DOMContentLoaded", function() {
    const addAuthorButton = document.getElementById("add-author-btn");
    const authorCountField = document.getElementById("author-count");

    addAuthorButton.addEventListener("click", function() {
        const authorIndex = parseInt(authorCountField.value);
        if (authorIndex < 3) {
            const authorElement = document.getElementById("author" + authorIndex);
            authorElement.hidden = false;
            authorCountField.value = authorIndex + 1;
            if (authorIndex === 2) {
                addAuthorButton.hidden = true;
            }
        }
    });
});