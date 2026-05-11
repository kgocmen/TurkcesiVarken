let wordsPageData = [];

let wordsEditMode = false;

let wordsSort = {
    column: "name",
    direction: "asc"
};

function escapeHtml(value) {

    const div =
        document.createElement("div");

    div.textContent =
        value == null ? "" : String(value);

    return div.innerHTML;

}

function escapeJsString(value) {

    return String(value == null ? "" : value)
        .replace(/\\/g, "\\\\")
        .replace(/'/g, "\\'")
        .replace(/\n/g, "\\n")
        .replace(/\r/g, "\\r")
        .replace(/</g, "\\x3C")
        .replace(/>/g, "\\x3E");

}

function renderRating(rating) {

    const value =
        Number(rating || 0);

    if (value <= 0) {
        return "-";
    }

    return "⭐".repeat(value);

}

function sortWords(words) {

    return [...words]
        .sort((a, b) => {

            const column =
                wordsSort.column;

            const direction =
                wordsSort.direction === "asc" ? 1 : -1;

            const aValue =
                column === "rating"
                    ? Number(a[column] || 0)
                    : String(a[column] || "")
                        .toLocaleLowerCase("tr");

            const bValue =
                column === "rating"
                    ? Number(b[column] || 0)
                    : String(b[column] || "")
                        .toLocaleLowerCase("tr");

            if (aValue < bValue) {
                return -1 * direction;
            }

            if (aValue > bValue) {
                return direction;
            }

            return 0;

        });

}

function sortIndicator(column) {

    if (wordsSort.column !== column) {
        return "";
    }

    return wordsSort.direction === "asc"
        ? " ↑"
        : " ↓";

}

function renderSortableHeader(column, label) {

    return `
        <button
            class="table-sort-button"
            type="button"
            onclick="sortWordsBy('${column}')"
        >
            ${label}${sortIndicator(column)}
        </button>
    `;

}

function renderWordTable(
        words,
        options = {}
) {

    const editable =
        Boolean(options.editable);

    const sortable =
        Boolean(options.sortable);

    if (!words || words.length === 0) {

        return `
            <div class="search-empty">
                Henüz kelime yok.
            </div>
        `;

    }

    const rows =
        words
            .map(word => `
                <tr data-word-id="${escapeHtml(word.id)}">
                    <td>
                        ${editable
                            ? `
                                <input
                                    class="table-edit-input"
                                    data-field="name"
                                    value="${escapeHtml(word.name)}"
                                >
                            `
                            : `
                                <a
                                    class="word-link"
                                    href="/word.html?slug=${encodeURIComponent(word.slug)}"
                                >
                                    ${escapeHtml(word.name)}
                                </a>
                            `
                        }
                    </td>
                    <td class="muted-cell">
                        ${editable
                            ? `
                                <input
                                    class="table-edit-input"
                                    data-field="slug"
                                    value="${escapeHtml(word.slug)}"
                                >
                            `
                            : escapeHtml(word.slug)
                        }
                    </td>
                    <td class="muted-cell">
                        ${editable
                            ? `
                                <input
                                    class="table-edit-input"
                                    data-field="origin"
                                    value="${escapeHtml(word.origin || "")}"
                                >
                            `
                            : escapeHtml(word.origin || "-")
                        }
                    </td>
                    <td class="muted-cell">
                        ${editable
                            ? `
                                <input
                                    class="table-edit-input"
                                    data-field="category"
                                    value="${escapeHtml(word.category || "")}"
                                >
                            `
                            : escapeHtml(word.category || "-")
                        }
                    </td>
                    <td class="rating-cell">
                        ${editable
                            ? `
                                <input
                                    class="table-edit-input table-rating-input"
                                    data-field="rating"
                                    type="number"
                                    min="0"
                                    max="5"
                                    value="${escapeHtml(word.rating || 0)}"
                                >
                            `
                            : renderRating(word.rating)
                        }
                    </td>
                </tr>
            `)
            .join("");

    return `
        <table class="word-table">
            <thead>
                <tr>
                    <th>
                        ${sortable ? renderSortableHeader("name", "Kelime") : "Kelime"}
                    </th>
                    <th>
                        ${sortable ? renderSortableHeader("slug", "Slug") : "Slug"}
                    </th>
                    <th>
                        ${sortable ? renderSortableHeader("origin", "Köken") : "Köken"}
                    </th>
                    <th>
                        ${sortable ? renderSortableHeader("category", "Kategori") : "Kategori"}
                    </th>
                    <th>
                        ${sortable ? renderSortableHeader("rating", "Rating") : "Rating"}
                    </th>
                </tr>
            </thead>
            <tbody>
                ${rows}
            </tbody>
        </table>
    `;

}

function renderWordsPageTable() {

    const container =
        document.getElementById(
            "wordsTable"
        );

    if (!container) {
        return;
    }

    container.innerHTML =
        renderWordTable(
            sortWords(wordsPageData),
            {
                editable: wordsEditMode,
                sortable: true
            }
        );

}

async function loadWordsPage() {

    const container =
        document.getElementById(
            "wordsTable"
        );

    if (!container) {
        return;
    }

    try {

        const response =
            await fetch("/api/words/all");

        if (!response.ok) {
            throw new Error("Kelimeler alınamadı.");
        }

        const words =
            await response.json();

        wordsPageData =
            words;

        renderWordsPageTable();

    } catch (error) {

        console.error(error);

        container.innerHTML = `
            <div class="search-empty">
                Kelimeler yüklenirken hata oluştu.
            </div>
        `;

    }

}

function sortWordsBy(column) {

    if (wordsSort.column === column) {

        wordsSort.direction =
            wordsSort.direction === "asc"
                ? "desc"
                : "asc";

    } else {

        wordsSort = {
            column,
            direction: "asc"
        };

    }

    renderWordsPageTable();

}

function toggleWordsEditMode() {

    wordsEditMode =
        !wordsEditMode;

    const button =
        document.getElementById(
            "editWordsButton"
        );

    const saveButton =
        document.getElementById(
            "saveWordsButton"
        );

    if (button) {

        button.textContent =
            wordsEditMode
                ? "Düzenleme Modu: Açık"
                : "Düzenleme Modu: Kapalı";

        button.classList.toggle(
            "is-active",
            wordsEditMode
        );

    }

    if (saveButton) {

        saveButton.style.display =
            wordsEditMode
                ? "inline-flex"
                : "none";

    }

    renderWordsPageTable();

}

function readWordRow(id) {

    const row =
        document.querySelector(
            `tr[data-word-id="${CSS.escape(id)}"]`
        );

    if (!row) {
        return null;
    }

    const data = {};

    row.querySelectorAll("[data-field]")
        .forEach(input => {
            data[input.dataset.field] =
                input.value.trim();
        });

    return data;

}

function getOriginalWord(id) {

    return wordsPageData.find(word =>
        word.id === id
    );

}

function isWordRowChanged(id, data) {

    const originalWord =
        getOriginalWord(id);

    if (!originalWord || !data) {
        return false;
    }

    return data.name !== String(originalWord.name || "")
            || data.slug !== String(originalWord.slug || "")
            || data.origin !== String(originalWord.origin || "")
            || data.category !== String(originalWord.category || "")
            || Number(data.rating || 0) !== Number(originalWord.rating || 0);

}

async function saveWordRow(id) {

    const data =
        readWordRow(id);

    if (!data) {
        return null;
    }

    try {

        const response =
            await fetch(
                `/api/words/${encodeURIComponent(id)}/summary`,
                {
                    method: "PATCH",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify(data)
                }
            );

        if (!response.ok) {
            throw new Error("Kelime güncellenemedi.");
        }

        const savedWord =
            await response.json();

        return savedWord;

    } catch (error) {

        console.error(error);

        throw error;

    }

}

async function saveAllWordRows() {

    const changedRows =
        Array.from(
            document.querySelectorAll("tr[data-word-id]")
        )
            .map(row => {

                const id =
                    row.dataset.wordId;

                const data =
                    readWordRow(id);

                return {
                    id,
                    data
                };

            })
            .filter(row =>
                isWordRowChanged(
                    row.id,
                    row.data
                )
            );

    if (changedRows.length === 0) {

        alert("Kaydedilecek değişiklik yok.");

        return;

    }

    try {

        const savedWords =
            await Promise.all(
                changedRows.map(row =>
                    saveWordRow(row.id)
                )
            );

        wordsPageData =
            wordsPageData.map(word =>
                savedWords.find(
                    savedWord =>
                        savedWord && savedWord.id === word.id
                ) || word
            );

        renderWordsPageTable();

    } catch (error) {

        alert("Değişiklikler kaydedilirken hata oluştu.");

    }

}

async function loadCategoriesPage() {

    const container =
        document.getElementById(
            "categoryAccordion"
        );

    if (!container) {
        return;
    }

    try {

        const response =
            await fetch("/api/words/categories");

        if (!response.ok) {
            throw new Error("Kategoriler alınamadı.");
        }

        const categories =
            await response.json();

        if (categories.length === 0) {

            container.innerHTML = `
                <div class="search-empty">
                    Henüz kategori yok.
                </div>
            `;

            return;

        }

        container.innerHTML =
            categories
                .map((category, index) => `
                    <section
                        class="accordion-item ${index === 0 ? "is-open" : ""}"
                    >
                        <div class="accordion-header">
                            <button
                                class="accordion-toggle"
                                type="button"
                                onclick="toggleCategory(this)"
                            >
                                <span>
                                    ${escapeHtml(category.name)}
                                </span>
                                <span class="accordion-count">
                                    ${category.words.length} kelime
                                </span>
                            </button>
                            <div class="category-actions">
                                <button
                                    class="small-action-button"
                                    type="button"
                                    onclick="renameCategory(
                                        '${escapeJsString(category.id)}',
                                        '${escapeJsString(category.name)}'
                                    )"
                                >
                                    İsmi Düzenle
                                </button>
                                <button
                                    class="small-action-button danger-button"
                                    type="button"
                                    onclick="deleteCategory(
                                        '${escapeJsString(category.id)}',
                                        '${escapeJsString(category.name)}'
                                    )"
                                >
                                    Kategoriyi Kaldır
                                </button>
                            </div>
                        </div>
                        <div class="accordion-panel">
                            <div class="table-wrap">
                                ${renderWordTable(category.words)}
                            </div>
                        </div>
                    </section>
                `)
                .join("");

    } catch (error) {

        console.error(error);

        container.innerHTML = `
            <div class="search-empty">
                Kategoriler yüklenirken hata oluştu.
            </div>
        `;

    }

}

async function createCategory() {

    const name =
        prompt("Kategori adı:");

    if (name == null) {
        return;
    }

    const trimmedName =
        name.trim();

    if (!trimmedName) {

        alert("Kategori adı boş olamaz.");

        return;

    }

    try {

        const response =
            await fetch(
                "/api/categories",
                {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        name: trimmedName
                    })
                }
            );

        if (!response.ok) {
            throw new Error("Kategori oluşturulamadı.");
        }

        await loadCategoriesPage();

    } catch (error) {

        console.error(error);

        alert("Kategori oluşturulurken hata oluştu.");

    }

}

async function renameCategory(id, currentName) {

    const name =
        prompt(
            "Yeni kategori adı:",
            currentName
        );

    if (name == null) {
        return;
    }

    const trimmedName =
        name.trim();

    if (!trimmedName) {

        alert("Kategori adı boş olamaz.");

        return;

    }

    try {

        const response =
            await fetch(
                `/api/categories/${encodeURIComponent(id)}`,
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json"
                    },
                    body: JSON.stringify({
                        name: trimmedName
                    })
                }
            );

        if (!response.ok) {
            throw new Error("Kategori düzenlenemedi.");
        }

        await loadCategoriesPage();

    } catch (error) {

        console.error(error);

        alert("Kategori düzenlenirken hata oluştu.");

    }

}

async function deleteCategory(id, name) {

    const confirmed =
        confirm(
            `"${name}" kategorisi silinsin mi? İçindeki kelimeler "Diğer" kategorisine taşınacak.`
        );

    if (!confirmed) {
        return;
    }

    try {

        const response =
            await fetch(
                `/api/categories/${encodeURIComponent(id)}`,
                {
                    method: "DELETE"
                }
            );

        if (!response.ok) {
            throw new Error("Kategori silinemedi.");
        }

        await loadCategoriesPage();

    } catch (error) {

        console.error(error);

        alert("Kategori silinirken hata oluştu.");

    }

}

function toggleCategory(button) {

    const item =
        button.closest(
            ".accordion-item"
        );

    if (!item) {
        return;
    }

    item.classList.toggle("is-open");

}

window.addEventListener(
    "DOMContentLoaded",
    () => {
        loadWordsPage();
        loadCategoriesPage();
    }
);
