async function getWord(slug) {

    const response =
        await fetch(`/words/${slug}`);

    return await response.json();
}

const API = {

    async createWord(data) {

        const response = await fetch("/words", {
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