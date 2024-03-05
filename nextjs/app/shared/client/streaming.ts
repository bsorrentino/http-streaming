
/**
 * Generator function that streams the response body from a fetch request.
 * 
 * @param {RequestInfo | URL} input - The input to fetch. Can be a Request or URL object. 
 * @param {RequestInit} [init] - Optional fetch init options.
 * @returns {AsyncGenerator<string, void, undefined>} An async generator that yields decoded response body strings.
 */
export async function* streamingFetch( input: RequestInfo | URL, init?: RequestInit ) {

    const response = await fetch( input, init)

    if( !response.ok ) throw `ERROR - status: ${response.statusText}`
    if( !response.body ) throw `ERROR - response body is null or undefined!` 
  
    const reader  = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
  
    for( ;; ) {
        const { done, value } = await reader.read()
        if( done ) break;

        try {
            yield decoder.decode(value)
        }
        catch( e:any ) {
            console.warn( e.message )
        }
      
    }
}

