async function searchWord() {

    const input =
        document.getElementById(
            "searchInput"
        );

    const value =
        input.value
            .trim()
            .toLowerCase();

    if (!value) {
        return;
    }

    window.location.href =
        `/word.html?slug=${value}`;

}