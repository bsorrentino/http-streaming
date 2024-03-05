
/**
 * A custom Response subclass that accepts a ReadableStream.
 * This allows creating a streaming Response for async generators.
 */
class StreamingResponse extends Response {
  /**
   * Constructor
   * 
   * @param {ReadableStream<any>} res - The ReadableStream
   * @param {ResponseInit} [init] - Optional ResponseInit overrides
   */
  constructor(
    res: ReadableStream<any>,
    init?: ResponseInit,

  ) {
    super(res as any, {
      ...init,
      status: 200,
      headers: {
        ...init?.headers,
      },
    });
  }
}

/**
 * Converts a string to a Uint8Array.
 *
 * @param {string} str - The string to convert.
 * @returns {Uint8Array} The Uint8Array representation of the string.
 */
const stringToUint8Array = (str: string) => {
  const encoder = new TextEncoder();
  return encoder.encode(str);
}

/**
 * Creates a streaming text Response from an async generator.
 * 
 * @param {AsyncGenerator<string, void, unknown>} generator - The async generator function
 * @returns {Response} The streaming text Response
 */ 
export const streamingTextResponse = (generator: AsyncGenerator<string, void, unknown>): Response => {

  const stream = new ReadableStream<any>({
    async start(controller) {
      for await (let chunk of generator) {
        const chunkData = stringToUint8Array(chunk);
        controller.enqueue(chunkData);
      }
      controller.close();
    }
  });
  return new StreamingResponse(stream, { headers: { 'Content-Type': 'text/plain; charset=utf-8' } })

}

export const streamingJsonResponse = <T extends Record<string, unknown>>(generator: AsyncGenerator<T, void, unknown>): Response => {

  const stream = new ReadableStream<any>({
    async start(controller) {
      for await (let chunk of generator) {
        const chunkData = stringToUint8Array(JSON.stringify(chunk));
        controller.enqueue(chunkData);
      }
      controller.close();
    }
  });

  return new StreamingResponse(stream, { headers: { 'Content-Type': 'application/json; charset=utf-8' } })
}
