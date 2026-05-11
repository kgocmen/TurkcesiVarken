let selectedRating = 0;

let selectedSynonyms = [];

function escapeHtml(value) {

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");

}

function escapeJsString(value) {

    return String(value)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\n/g, "\\n")
        .replace(/\r/g, "\\r")
        .replace(/</g, "\\x3C")
        .replace(/>/g, "\\x3E");

}

/*
--------------------------------
RATING
--------------------------------
*/

function setRating(value) {

    selectedRating = value;

    const stars =
        document.querySelectorAll(
            "#ratingStars span"
        );

    stars.forEach((star, index) => {

        star.innerText =
            index < value ? "⭐" : "☆";

    });

}

/*
--------------------------------
SEARCH CATEGORIES
--------------------------------
*/

async function searchCategories() {

    const input =
        document.getElementById(
            "category"
        );

    const results =
        document.getElementById(
            "categoryResults"
        );

    if (!input || !results) {
        return;
    }

    const query =
        input.value.trim();

    if (query.length < 1) {

        results.innerHTML = "";

        return;

    }

    try {

        const response =
            await fetch("/api/categories");

        if (!response.ok) {
            throw new Error("Kategoriler alınamadı.");
        }

        const categories =
            await response.json();

        const matchingCategories =
            categories
                .filter(category =>
                    category.name
                        .toLocaleLowerCase("tr")
                        .includes(
                            query.toLocaleLowerCase("tr")
                        )
                )
                .sort((a, b) =>
                    a.name.localeCompare(
                        b.name,
                        "tr",
                        { sensitivity: "base" }
                    )
                );

        results.innerHTML = "";

        matchingCategories
            .forEach(category => {

                results.innerHTML += `
                    <button
                        type="button"
                        onclick="selectCategory(
                            '${escapeJsString(category.name)}'
                        )"
                        style="
                            background:rgba(255,255,255,0.06);
                            border:1px solid rgba(255,255,255,0.08);
                            color:white;
                            padding:12px 16px;
                            border-radius:14px;
                            cursor:pointer;
                            text-align:left;
                            transition:.2s ease;
                        "
                    >
                        ${escapeHtml(category.name)}
                    </button>
                `;

            });

        const exactMatch =
            matchingCategories.some(category =>
                category.name.localeCompare(
                    query,
                    "tr",
                    { sensitivity: "base" }
                ) === 0
            );

        if (!exactMatch) {

            results.innerHTML += `
                <button
                    type="button"
                    onclick="selectCategoryFromInput()"
                    style="
                        background:rgba(80, 180, 140, 0.16);
                        border:1px solid rgba(80, 180, 140, 0.28);
                        color:white;
                        padding:12px 16px;
                        border-radius:14px;
                        cursor:pointer;
                        text-align:left;
                        transition:.2s ease;
                    "
                >
                    Yeni kategori olarak kullan: ${escapeHtml(query)}
                </button>
            `;

        }

    } catch (e) {

        console.error(e);

    }

}

function selectCategory(name) {

    document.getElementById(
        "category"
    ).value = name.trim();

    document.getElementById(
        "categoryResults"
    ).innerHTML = "";

}

function selectCategoryFromInput() {

    const input =
        document.getElementById(
            "category"
        );

    selectCategory(
        input.value
    );

}

function getSelectedCategory() {

    const input =
        document.getElementById(
            "category"
        );

    const name =
        input ? input.value.trim() : "";

    if (!name) {
        return null;
    }

    return {
        name
    };

}

/*
--------------------------------
SEARCH SYNONYMS
--------------------------------
*/

async function searchSynonyms() {

    const query =
        document.getElementById(
            "synonymSearch"
        ).value.trim();

    const results =
        document.getElementById(
            "synonymResults"
        );

    if (query.length < 2) {

        results.innerHTML = "";

        return;

    }

    try {

        const response =
            await fetch(
                `/api/words/search?q=${encodeURIComponent(query)}`
            );

        const words =
            await response.json();

        results.innerHTML = "";

        const sortedWords =
            words
            .sort((a, b) =>
                a.name.localeCompare(
                    b.name,
                    "tr",
                    { sensitivity: "base" }
                )
            );

        sortedWords
            .forEach(word => {

                results.innerHTML += `
                    <button
                        type="button"

                        onclick="addSynonym(
                            '${escapeJsString(word.id)}',
                            '${escapeJsString(word.name)}'
                        )"

                        style="
                            background:rgba(255,255,255,0.06);

                            border:1px solid rgba(255,255,255,0.08);

                            color:white;

                            padding:12px 16px;

                            border-radius:14px;

                            cursor:pointer;

                            text-align:left;

                            transition:.2s ease;
                        "
                    >
                        ${escapeHtml(word.name)}
                    </button>
                `;

            });

        const exactMatch =
            sortedWords.some(word =>
                word.name.localeCompare(
                    query,
                    "tr",
                    { sensitivity: "base" }
                ) === 0
            );

        if (!exactMatch) {

            results.innerHTML += `
                <button
                    type="button"

                    onclick="addNewSynonymFromSearch()"

                    style="
                        background:rgba(80, 180, 140, 0.16);

                        border:1px solid rgba(80, 180, 140, 0.28);

                        color:white;

                        padding:12px 16px;

                        border-radius:14px;

                        cursor:pointer;

                        text-align:left;

                        transition:.2s ease;
                    "
                >
                    Yeni kelime olarak ekle: ${escapeHtml(query)}
                </button>
            `;

        }

    } catch (e) {

        console.error(e);

    }

}

/*
--------------------------------
ADD SYNONYM
--------------------------------
*/

function addSynonym(id, name) {

    const normalizedName =
        name.trim();

    const synonymKey =
        id || normalizedName.toLocaleLowerCase("tr");

    const exists =
        selectedSynonyms.find(
            word => word.key === synonymKey
                    || (
                        word.name.toLocaleLowerCase("tr")
                            === normalizedName.toLocaleLowerCase("tr")
                    )
        );

    if (exists) {
        return;
    }

    selectedSynonyms.push({
        key: synonymKey,
        id,
        name: normalizedName
    });

    document.getElementById(
        "synonymSearch"
    ).value = "";

    document.getElementById(
        "synonymResults"
    ).innerHTML = "";

    renderSynonyms();

}

function addNewSynonymFromSearch() {

    const input =
        document.getElementById(
            "synonymSearch"
        );

    const name =
        input.value.trim();

    if (name.length < 2) {
        return;
    }

    addSynonym(
        null,
        name
    );

}

/*
--------------------------------
REMOVE SYNONYM
--------------------------------
*/

function removeSynonym(id) {

    selectedSynonyms =
        selectedSynonyms.filter(
            word => word.key !== id
        );

    renderSynonyms();

}

/*
--------------------------------
RENDER SYNONYMS
--------------------------------
*/

function renderSynonyms() {

    const container =
        document.getElementById(
            "selectedSynonyms"
        );

    container.innerHTML = "";

    selectedSynonyms.forEach(word => {

        container.innerHTML += `

            <div
                style="
                    display:flex;
                    align-items:center;
                    gap:10px;

                    background:rgba(255,255,255,0.08);

                    border:1px solid rgba(255,255,255,0.08);

                    padding:8px 12px;

                    border-radius:999px;

                    font-size:14px;
                "
            >

                <span>
                    ${escapeHtml(word.name)}
                </span>

                <button
                    type="button"

                    onclick="removeSynonym(
                        '${escapeJsString(word.key)}'
                    )"

                    style="
                        border:none;
                        background:none;

                        color:#ff6b6b;

                        cursor:pointer;

                        font-size:14px;
                    "
                >
                    ✕
                </button>

            </div>

        `;

    });

}

/*
--------------------------------
CREATE WORD
--------------------------------
*/

async function createWord() {

    const data = {

        name:
        document.getElementById(
            "name"
        ).value,

        definition:
        document.getElementById(
            "definition"
        ).value,

        origin:
        document.getElementById(
            "origin"
        ).value,

        notes:
        document.getElementById(
            "notes"
        ).value,

        rating:
        selectedRating,

        tdk:
        document.getElementById(
            "tdk"
        ).checked,

        category:
            getSelectedCategory(),

        synonyms:
            selectedSynonyms.map(
                word => ({
                    id: word.id,
                    name: word.name
                })
            ),
        similarWords:
            document.getElementById(
                "similarWords"
            ).value
                .split(",")
                .map(word => word.trim())
                .filter(word => word.length > 0)

    };

    try {

        const savedWord =
            await API.createWord(data);


        window.location.href =
            `/word.html?slug=${savedWord.slug}`;

    } catch (e) {

        console.error(e);

        alert("Bir hata oluştu.");

    }

}

const params =
    new URLSearchParams(
        window.location.search
    );

const wordId =
    params.get("id");

const initialWord =
    params.get("word");


window.addEventListener(
    "DOMContentLoaded",
    async () => {

        const synonymSearch =
            document.getElementById(
                "synonymSearch"
            );

        if (synonymSearch) {

            synonymSearch.addEventListener(
                "keydown",
                event => {

                    if (event.key !== "Enter") {
                        return;
                    }

                    event.preventDefault();
                    addNewSynonymFromSearch();

                }
            );

        }

        const categoryInput =
            document.getElementById(
                "category"
            );

        if (categoryInput) {

            categoryInput.addEventListener(
                "keydown",
                event => {

                    if (event.key !== "Enter") {
                        return;
                    }

                    event.preventDefault();
                    selectCategoryFromInput();

                }
            );

        }

        if (initialWord) {

            document.getElementById(
                "name"
            ).value =
                initialWord;

        }

        if (!wordId) {
            return;
        }

        try {

            const response =
                await fetch(
                    `/api/words/id/${wordId}`
                );

            const word =
                await response.json();

            document.getElementById(
                "editTitle"
            ).innerText =
                word.name;

            /*
            --------------------------------
            SYNONYMS
            --------------------------------
            */

            if (word.synonyms) {

                selectedSynonyms =
                    word.synonyms.map(
                        synonym => ({
                            id: synonym.id,
                            key: synonym.id,
                            name: synonym.name
                        })
                    );

                renderSynonyms();

            }

            /*
            --------------------------------
            SIMILAR WORDS
            --------------------------------
            */

            if (word.similarWords) {

                document.getElementById(
                    "similarWords"
                ).value =
                    word.similarWords.join(", ");

            }

            document.getElementById(
                "name"
            ).value =
                word.name || "";

            document.getElementById(
                "definition"
            ).value =
                word.definition || "";

            document.getElementById(
                "origin"
            ).value =
                word.origin || "";

            document.getElementById(
                "notes"
            ).value =
                word.notes || "";

            document.getElementById(
                "category"
            ).value =
                word.category?.name || "";

            document.getElementById(
                "tdk"
            ).checked =
                word.tdk;

            if (word.rating) {
                setRating(word.rating);
            }

        } catch (e) {

            console.error(e);

        }

    }
);

/*
--------------------------------
UPDATE WORD
--------------------------------
*/

async function updateWord() {

    const data = {

        name:
        document.getElementById(
            "name"
        ).value,

        definition:
        document.getElementById(
            "definition"
        ).value,

        origin:
        document.getElementById(
            "origin"
        ).value,

        notes:
        document.getElementById(
            "notes"
        ).value,

        rating:
        selectedRating,

        tdk:
        document.getElementById(
            "tdk"
        ).checked,

        category:
            getSelectedCategory(),

        synonyms:
            selectedSynonyms.map(
                word => ({
                    id: word.id,
                    name: word.name
                })
            ),

        similarWords:
            document.getElementById(
                "similarWords"
            ).value
                .split(",")
                .map(word => word.trim())
                .filter(word => word.length > 0)

    };

    try {

        const response =
            await fetch(
                `/api/words/${wordId}`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify(data)
                }
            );

        const savedWord =
            await response.json();

        window.location.href =
            `/word.html?slug=${savedWord.slug}`;

    } catch (e) {

        console.error(e);

        alert("Bir hata oluştu.");

    }

}

/*
--------------------------------
DELETE WORD
--------------------------------
*/

async function deleteWord() {

    const confirmed =
        confirm(
            "Kelime silinsin mi?"
        );

    if (!confirmed) {
        return;
    }

    try {

        await fetch(
            `/api/words/${wordId}`,
            {
                method: "DELETE"
            }
        );

        window.location.href = "/";

    } catch (e) {

        console.error(e);

        alert("Bir hata oluştu.");

    }

}

/*
--------------------------------
FILL FROM NISANYAN
--------------------------------
*/

async function fillFromNisanyan() {

    const name =
        document.getElementById(
            "name"
        ).value;

    if (!name) {

        alert("Önce kelime adı gir.");

        return;

    }

    try {

        const response =
            await fetch(
                `/nisanyan/${name}`
            );

        if (!response.ok) {

            alert("Kelime bulunamadı.");

            return;

        }

        const data =
            await response.json();

        /*
        --------------------------------
        FILL FORM
        --------------------------------
        */

        document.getElementById(
            "definition"
        ).value =
            data.definition || "";

        document.getElementById(
            "origin"
        ).value =
            data.origin || "";

        document.getElementById(
            "notes"
        ).value =
            data.notes || "";

        /*
        --------------------------------
        SIMILAR WORDS
        --------------------------------
        */

        if (data.similarWords) {

            document.getElementById(
                "similarWords"
            ).value =
                data.similarWords.join(
                    ", "
                );

        }


    } catch (e) {

        console.error(e);

        alert("Bir hata oluştu.");

    }

}
