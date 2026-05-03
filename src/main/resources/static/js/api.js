async function getWord(slug) {

    const response =
        await fetch(`/api/words/${slug}`);

    return await response.json();
}

const API = {

    async createWord(data) {

        const response = await fetch("/api/words", {
            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(data)
        });

        if (!response.ok) {
            throw new Error("Kelime oluşturulamadı.");
        }

        return await response.json();

    }

};