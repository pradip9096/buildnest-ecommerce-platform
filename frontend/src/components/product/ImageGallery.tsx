import { useState } from 'react';

interface Props {
  imageUrl?: string;
  productName: string;
}

export function ImageGallery({ imageUrl, productName }: Props) {
  const [selected, setSelected] = useState(0);
  const images = imageUrl ? [imageUrl] : [];

  return (
    <div className="flex flex-col gap-3">
      <div className="aspect-square rounded-2xl bg-gray-100 border border-gray-200 overflow-hidden flex items-center justify-center">
        {images[selected] ? (
          <img
            src={images[selected]}
            alt={productName}
            className="w-full h-full object-cover"
          />
        ) : (
          <span className="text-gray-300 text-8xl">🏗️</span>
        )}
      </div>

      {images.length > 0 && (
        <div className="flex gap-2">
          {images.map((src, i) => (
            <button
              key={i}
              onClick={() => setSelected(i)}
              className={`w-16 h-16 rounded-lg border-2 overflow-hidden flex-shrink-0 ${
                selected === i ? 'border-primary-500' : 'border-gray-200 hover:border-gray-400'
              }`}
            >
              <img src={src} alt={`${productName} thumbnail ${i + 1}`} className="w-full h-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
