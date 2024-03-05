import { streamingJsonResponse } from "@/app/shared/server/streaming";
import { NextRequest } from "next/server";
/**
 * Exports a constant string specifying which Next.js runtime to use.
 * 'edge' runs on Vercel Edge Network.
 */
// export const runtime = 'edge';

/**
 * Exports a constant string specifying which Next.js runtime to use.
 * 'dynamic' forces all pages to be Server Components.
 */
export const dynamic = 'force-dynamic';

const sleep = async (ms: number) => 
  (new Promise(resolve => setTimeout(resolve, ms)))

type Item = {
  key: string;
  value: string;
}
async function *fetchItems(): AsyncGenerator<Item, void, unknown> {
  
  for( let i = 0 ; i < 10 ; ++i ) {
    await sleep(1000)
    yield {
      key: `key${i}`,
      value: `value${i}`
    }
  }
}

export async function GET(req: NextRequest ) {

  return streamingJsonResponse( fetchItems() )
}

