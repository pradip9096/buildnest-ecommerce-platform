import { useRef, useState } from 'react';
import { useAsync } from '../../hooks/useAsync';
import {
  fetchProductImages,
  uploadProductImage,
  reorderProductImages,
  deleteProductImage,
  type AdminProduct,
  type AdminProductImage,
} from '../../api/admin';

interface Props {
  product: AdminProduct;
  onClose: () => void;
}

const ALLOWED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp', 'image/gif']);
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB — mirrors LocalStorageService's server-side limit

function validateFile(file: File): string | null {
  if (!ALLOWED_TYPES.has(file.type)) {
    return 'Unsupported file type. Allowed: JPEG, PNG, WebP, GIF.';
  }
  if (file.size > MAX_FILE_SIZE) {
    return 'File exceeds the maximum allowed size of 10 MB.';
  }
  return null;
}

export function ProductImagesModal({ product, onClose }: Props) {
  const { data, loading, error, setData } = useAsync<AdminProductImage[]>(
    () => fetchProductImages(product.id),
    [product.id]
  );
  const images = data ?? [];
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [actionError, setActionError] = useState<string | null>(null);
  const [busyImageId, setBusyImageId] = useState<number | null>(null);

  const sorted = [...images].sort((a, b) => a.displayOrder - b.displayOrder);

  const handleFileSelected = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file) return;

    const validationError = validateFile(file);
    if (validationError) {
      setActionError(validationError);
      return;
    }

    setUploading(true);
    setActionError(null);
    try {
      const uploaded = await uploadProductImage(product.id, file);
      setData(prev => [...(prev ?? []), uploaded]);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Failed to upload image');
    } finally {
      setUploading(false);
    }
  };

  const handleMove = async (imageId: number, direction: -1 | 1) => {
    const index = sorted.findIndex(img => img.id === imageId);
    const targetIndex = index + direction;
    if (index === -1 || targetIndex < 0 || targetIndex >= sorted.length) return;

    const reordered = [...sorted];
    [reordered[index], reordered[targetIndex]] = [reordered[targetIndex], reordered[index]];
    const imageIds = reordered.map(img => img.id);

    setBusyImageId(imageId);
    setActionError(null);
    try {
      const updated = await reorderProductImages(product.id, imageIds);
      setData(updated);
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Failed to reorder images');
    } finally {
      setBusyImageId(null);
    }
  };

  const handleDelete = async (image: AdminProductImage) => {
    if (!confirm('Delete this image? This cannot be undone.')) return;
    setBusyImageId(image.id);
    setActionError(null);
    try {
      await deleteProductImage(product.id, image.id);
      setData(prev => (prev ?? []).filter(img => img.id !== image.id));
    } catch (err) {
      setActionError(err instanceof Error ? err.message : 'Failed to delete image');
    } finally {
      setBusyImageId(null);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-sm p-4">
      <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl max-h-[90vh] overflow-y-auto">
        <div className="flex items-center justify-between px-6 pt-5 pb-4 border-b border-gray-100">
          <h3 className="font-semibold text-gray-900">Images — {product.name}</h3>
          <button type="button" onClick={onClose} className="text-gray-400 hover:text-gray-600 text-xl leading-none">&times;</button>
        </div>

        <div className="px-6 py-4 space-y-4">
          <div>
            <input
              ref={fileInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={handleFileSelected}
              disabled={uploading}
              aria-label="Upload image"
              className="text-sm"
            />
            {uploading && <p className="text-xs text-gray-400 mt-1">Uploading…</p>}
          </div>

          {error && <p className="text-red-600 text-sm">{error}</p>}
          {actionError && <p className="text-red-600 text-sm">{actionError}</p>}

          {loading ? (
            <p className="text-sm text-gray-400">Loading images…</p>
          ) : sorted.length === 0 ? (
            <p className="text-sm text-gray-400">No images uploaded yet.</p>
          ) : (
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-4">
              {sorted.map((image, index) => (
                <div key={image.id} className="border border-gray-100 rounded-xl p-2 space-y-2">
                  <div className="relative">
                    <img src={image.imageUrl} alt={image.altText ?? product.name} className="w-full h-28 object-cover rounded-lg" />
                    {image.isPrimary && (
                      <span className="absolute top-1 left-1 bg-primary-500 text-white text-[10px] font-semibold px-1.5 py-0.5 rounded">
                        Primary
                      </span>
                    )}
                  </div>
                  <div className="flex items-center justify-between gap-1">
                    <div className="flex gap-1">
                      <button
                        type="button"
                        onClick={() => handleMove(image.id, -1)}
                        disabled={index === 0 || busyImageId === image.id}
                        aria-label="Move earlier"
                        className="text-xs border border-gray-200 rounded px-2 py-0.5 disabled:opacity-40 hover:bg-gray-50"
                      >
                        ↑
                      </button>
                      <button
                        type="button"
                        onClick={() => handleMove(image.id, 1)}
                        disabled={index === sorted.length - 1 || busyImageId === image.id}
                        aria-label="Move later"
                        className="text-xs border border-gray-200 rounded px-2 py-0.5 disabled:opacity-40 hover:bg-gray-50"
                      >
                        ↓
                      </button>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleDelete(image)}
                      disabled={busyImageId === image.id}
                      className="text-xs font-medium text-red-600 hover:text-red-800 disabled:opacity-50"
                    >
                      Delete
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
