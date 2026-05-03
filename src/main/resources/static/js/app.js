async function searchWord() {

    const input =
        document.getElementById(
            "searchInput"
        );

    const query =
        input.value
            .trim();

    if (!query) {
        return;
    }

    try {

        const words =
            await fetchSearchResults(query);

        const existingWord =
            words.find(word =>
                word.name
                    .trim()
                    .toLocaleLowerCase("tr-TR") === query
                    .toLocaleLowerCase("tr-TR")
            );

        if (existingWord) {

            window.location.href =
                `/word.html?slug=${existingWord.slug}`;

            return;

        }

        window.location.href =
            `/admin/create.html?word=${encodeURIComponent(query)}`;

    } catch (error) {

        window.location.href =
            `/admin/create.html?word=${encodeURIComponent(query)}`;

    }

}

async function searchExistingWords() {

    const input =
        document.getElementById(
            "searchInput"
        );

    const results =
        document.getElementById(
            "searchResults"
        );

    const query =
        input.value
            .trim();

    if (query.length < 2) {

        results.innerHTML = "";
        results.classList.remove("is-visible");

        return;

    }

    try {

        const words =
            await fetchSearchResults(query);

        if (words.length === 0) {

            results.innerHTML =
                `
                    <a
                        class="search-result-item"
                        href="/admin/create.html?word=${encodeURIComponent(query)}"
                    >
                        "${escapeHtml(query)}" kelimesini oluştur
                    </a>
                `;

            results.classList.add("is-visible");

            return;

        }

        results.innerHTML =
            words
                .map(word => `
                    <a
                        class="search-result-item"
                        href="/word.html?slug=${word.slug}"
                    >
                        ${escapeHtml(word.name)}
                    </a>
                `)
                .join("");

        results.classList.add("is-visible");

    } catch (error) {

        results.innerHTML =
            `<div class="search-empty">Arama sırasında hata oluştu.</div>`;

        results.classList.add("is-visible");

    }

}

async function fetchSearchResults(query) {

    const response =
        await fetch(
            `/api/words/search?q=${encodeURIComponent(query)}`
        );

    if (!response.ok) {
        throw new Error("Arama yapılamadı.");
    }

    return await response.json();

}

function handleSearchKeydown(event) {

    if (event.key === "Enter") {
        searchWord();
    }

}

function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value;

    return div.innerHTML;

}
