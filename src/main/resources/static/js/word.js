const params =
    new URLSearchParams(
        window.location.search
    );

const slug =
    params.get("slug");

/*
--------------------------------
LOAD WORD
--------------------------------
*/

async function loadWord() {

    if (!slug) {
        return;
    }

    try {

        const response =
            await fetch(
                `/api/words/${slug}`
            );

        /*
        --------------------------------
        NOT FOUND
        --------------------------------
        */

        if (!response.ok) {

            document.body.innerHTML = `
                <div
                    style="
                        min-height:100vh;

                        display:flex;
                        align-items:center;
                        justify-content:center;

                        color:white;

                        font-size:28px;
                        font-family:sans-serif;
                    "
                >
                    Kelime bulunamadı.
                </div>
            `;

            return;

        }

        const word =
            await response.json();

        /*
        --------------------------------
        TITLE
        --------------------------------
        */

        document.title =
            word.name;

        document.getElementById(
            "wordTitle"
        ).innerText =
            word.name;

        /*
        --------------------------------
        EDIT BUTTON
        --------------------------------
        */

        document.getElementById(
            "editButton"
        ).href =
            `/admin/edit.html?id=${word.id}`;

        /*
        --------------------------------
        TDK
        --------------------------------
        */

        document.getElementById(
            "tdkBadge"
        ).innerText =
            word.tdk
                ? "✓ TDK Onaylı"
                : "Topluluk Sözcüğü";

        /*
        --------------------------------
        CONTENT
        --------------------------------
        */

        document.getElementById(
            "wordDefinition"
        ).innerText =
            word.definition || "-";

        document.getElementById(
            "wordOrigin"
        ).innerText =
            word.origin || "-";

        document.getElementById(
            "wordCategory"
        ).innerText =
            word.category?.name || "-";

        document.getElementById(
            "wordNotes"
        ).innerText =
            word.notes || "-";

        /*
        --------------------------------
        RATING
        --------------------------------
        */

        document.getElementById(
            "wordRating"
        ).innerHTML =
            "⭐".repeat(
                word.rating || 0
            );

        /*
        --------------------------------
        SYNONYMS
        --------------------------------
        */

        const synonymList =
            document.getElementById(
                "synonymList"
            );

        synonymList.innerHTML = "";

        if (
            word.synonyms &&
            word.synonyms.length > 0
        ) {

            word.synonyms
                .sort(
                    (a, b) =>
                        (b.rating || 0) -
                        (a.rating || 0)
                )
                .forEach(
                    synonym => {

                        synonymList.innerHTML += `
                    <a
                        href="/word.html?slug=${synonym.slug}"
                        class="pill"
                    >
                        ${synonym.name}
                        ${"⭐".repeat(synonym.rating || 0)}
                    </a>
                `;

                    }
                );

        } else {

            synonymList.innerHTML = `
        <div
            style="
                opacity:.6;
                font-size:15px;
            "
        >
            Henüz eş anlamlı sözcük yok.
        </div>
    `;

        }

        /*
--------------------------------
SIMILAR WORDS
--------------------------------
*/

        const similarList =
            document.getElementById(
                "similarList"
            );

        similarList.innerHTML = "";

        if (
            word.similarWords &&
            word.similarWords.length > 0
        ) {

            for (const similar of word.similarWords) {

                try {

                    const response =
                        await fetch(
                            `/api/words/${similar}`
                        );

                    if (response.ok) {

                        similarList.innerHTML += `
                    <a
                        href="/word.html?slug=${similar}"
                        class="pill"
                    >
                        ${similar}
                    </a>
                `;

                    } else {

                        similarList.innerHTML += `
                    <span class="pill">
                        ${similar}
                    </span>
                `;

                    }

                } catch {

                    similarList.innerHTML += `
                <span class="pill">
                    ${similar}
                </span>
            `;

                }

            }

        } else {

            similarList.innerHTML = `
        <div
            style="
                opacity:.6;
                font-size:15px;
            "
        >
            Henüz benzer sözcük yok.
        </div>
    `;

        }

    } catch (e) {

        console.error(e);

    }

}

/*
--------------------------------
INIT
--------------------------------
*/

window.addEventListener(
    "DOMContentLoaded",
    loadWord
);