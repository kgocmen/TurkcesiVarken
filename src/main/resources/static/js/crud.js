let selectedRating = 0;

let selectedSynonyms = [];

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
SEARCH SYNONYMS
--------------------------------
*/

async function searchSynonyms() {

    const query =
        document.getElementById(
            "synonymSearch"
        ).value;

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
                `/api/words/search?q=${query}`
            );

        const words =
            await response.json();

        results.innerHTML = "";

        words.forEach(word => {

            results.innerHTML += `
                <button
                    type="button"

                    onclick="addSynonym(
                        '${word.id}',
                        '${word.name}'
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
                    ${word.name}
                </button>
            `;

        });

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

    const exists =
        selectedSynonyms.find(
            word => word.id === id
        );

    if (exists) {
        return;
    }

    selectedSynonyms.push({
        id,
        name
    });

    document.getElementById(
        "synonymSearch"
    ).value = "";

    document.getElementById(
        "synonymResults"
    ).innerHTML = "";

    renderSynonyms();

}

/*
--------------------------------
REMOVE SYNONYM
--------------------------------
*/

function removeSynonym(id) {

    selectedSynonyms =
        selectedSynonyms.filter(
            word => word.id !== id
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
                    ${word.name}
                </span>

                <button
                    type="button"

                    onclick="removeSynonym(
                        '${word.id}'
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

        synonyms:
            selectedSynonyms.map(
                word => ({
                    id: word.id
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

        alert("Kelime oluşturuldu.");

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


window.addEventListener(
    "DOMContentLoaded",
    async () => {

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

        synonyms:
            selectedSynonyms.map(
                word => ({
                    id: word.id
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

        alert("Kelime silindi.");

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

        alert(
            "Önce kelime adı gir."
        );

        return;

    }

    try {

        const response =
            await fetch(
                `/nisanyan/${name}`
            );

        if (!response.ok) {

            alert(
                "Kelime bulunamadı."
            );

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

        alert(
            "Nişanyan verileri dolduruldu."
        );

    } catch (e) {

        console.error(e);

        alert(
            "Bir hata oluştu."
        );

    }

}