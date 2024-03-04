

/**
 * Generator function to stream responses from fetch calls.
 * 
 * @param {Function} fetchcall - The fetch call to make. Should return a response with a readable body stream.
 * @returns {AsyncGenerator<string>} An async generator that yields strings from the response stream.
 */
async function* streamingFetch(fetchcall) {

    const response = await fetchcall();

    const reader = response.body.getReader();
    while (true) {
        const { done, value } = await reader.read();
        if (done) break;
        yield (new TextDecoder().decode(value));
    }
}

(async () => {

    const div = document.getElementById('result');
    for await ( let chunk of streamingFetch( () => fetch('/stream') ) ) {
        console.log( chunk );
        div.innerHTML += `<li>${chunk}</li>`;
    }

})()

