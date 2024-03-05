'use client';

import {  Suspense } from 'react'
import RenderStreamData from './components/RenderStreamData';


export default function ReadingStreamingData() {

  return ( 
    <Suspense fallback={<div>loading...</div>}>
      <RenderStreamData/> 
    </Suspense>
  )
}
