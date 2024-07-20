
/**
 * Asynchronously waits for a specified number of milliseconds.
 * 
 * @param {number} ms - The number of milliseconds to wait.
 * @returns {Promise<void>} A promise that resolves after the specified delay.
 */
const delay = async (ms) => (new Promise(resolve => setTimeout(resolve, ms)));

/**
 * Asynchronously fetches data from a given fetch call and yields the data in chunks.
 * 
 * @async
 * @generator
 * @param Response - Response object to stream.
 * @yields {Promise<string>} The decoded text chunk from the response stream.
 */
async function* streamingResponse(response) {
  // Attach Reader
  const reader = response.body.getReader();
  while (true) {
    // wait for next encoded chunk
    const { done, value } = await reader.read();
    // check if stream is done
    if (done) break;
    // Decodes data chunk and yields it
    yield (new TextDecoder().decode(value));
  }
}

/**
 * StreamingElement is a custom web component .
 * It is used to display streaming data.
 * 
 * @class
 * @extends {HTMLElement}
 */
export class StreamingElement extends HTMLElement {

  /**
   * Styles applied to the component.
   * 
   * @static
   * @type {string}
   */
  static styles = ``

  get url() { return this.getAttribute('url') }

  /**
   * Creates an instance of LG4JInputElement.
   * 
   * @constructor
   */
  constructor() {
    super();

    // Create a shadow root
    this.attachShadow({ mode: 'open' });
    this.shadowRoot.innerHTML = `
      <style>
        ${StreamingElement.styles}
      </style>
      <ol>
      </ol>
    `;
  }

  /**
   * Lifecycle method called when the element is added to the document's DOM.
   */
  connectedCallback() {
    this.#fetchStreamingData()

  }

  render( chunk ) {
    
    const container = this.shadowRoot.querySelector('ol');

    const elem = document.createElement( 'li' );

    elem.innerText = chunk;

    container.appendChild( elem );
  }


  async #fetchStreamingData() {

    console.debug( 'start fetching data')
    const execResponse = await fetch(`${this.url}/stream`);

    for await (let chunk of streamingResponse( execResponse )  ) {
        console.debug( 'fetched chunk', chunk )
        this.render( chunk );
    }
  }

}


window.customElements.define('streaming-poc', StreamingElement);
