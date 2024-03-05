// pages/index.js
import { useEffect, useState } from 'react';
import { streamingFetch } from '../shared/client/streaming';

export default function RenderStreamData() {
  const [data, setData] = useState<any[]>([]);

  useEffect( () => {

    const asyncFetch = async ( ) => {

      const it = streamingFetch( '/api/stream-data') 

      for await ( let value of it ) {
        try {
          const chunk = JSON.parse(value);
          setData( (prev) => [...prev, chunk]);
        }
        catch( e:any ) {
          console.warn( e.message )
        }
      }
    }
    
    asyncFetch()
  }, []);

  return (
    <div>
        {data.map((chunk, index) => (
          <p key={index}>{`Received chunk ${index} - ${chunk.value}`}</p>
        ))}
    </div>
  );
}
