import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ProductImagesModal } from './ProductImagesModal';
import {
  fetchProductImages,
  uploadProductImage,
  reorderProductImages,
  deleteProductImage,
  type AdminProduct,
  type AdminProductImage,
} from '../../api/admin';

vi.mock('../../api/admin', () => ({
  fetchProductImages: vi.fn(),
  uploadProductImage: vi.fn(),
  reorderProductImages: vi.fn(),
  deleteProductImage: vi.fn(),
}));

const mockFetch = vi.mocked(fetchProductImages);
const mockUpload = vi.mocked(uploadProductImage);
const mockReorder = vi.mocked(reorderProductImages);
const mockDelete = vi.mocked(deleteProductImage);

const product: AdminProduct = {
  id: 1,
  name: 'Premium Cement 50kg',
  price: 499.99,
  isActive: true,
};

const imgA: AdminProductImage = { id: 10, imageUrl: '/uploads/a.jpg', displayOrder: 0, isPrimary: true };
const imgB: AdminProductImage = { id: 11, imageUrl: '/uploads/b.jpg', displayOrder: 1, isPrimary: false };

function jpegFile(name = 'photo.jpg', size = 1024): File {
  const file = new File([new Uint8Array(size)], name, { type: 'image/jpeg' });
  return file;
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('ProductImagesModal', () => {
  it('lists images ordered by displayOrder, with the primary badge shown', async () => {
    mockFetch.mockResolvedValue([imgB, imgA]);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);

    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(2));
    const imgs = screen.getAllByRole('img');
    expect(imgs[0]).toHaveAttribute('src', '/uploads/a.jpg');
    expect(imgs[1]).toHaveAttribute('src', '/uploads/b.jpg');
    expect(screen.getByText('Primary')).toBeInTheDocument();
  });

  it('shows the empty state when there are no images', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);

    await waitFor(() => expect(screen.getByText('No images uploaded yet.')).toBeInTheDocument());
  });

  it('uploads a valid image and appends it to the list', async () => {
    mockFetch.mockResolvedValue([imgA]);
    mockUpload.mockResolvedValue({ id: 12, imageUrl: '/uploads/c.jpg', displayOrder: 1, isPrimary: false });

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(1));

    const file = jpegFile();
    const user = userEvent.setup();
    await user.upload(screen.getByLabelText('Upload image'), file);

    await waitFor(() => expect(mockUpload).toHaveBeenCalledWith(1, file));
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(2));
  });

  it('rejects an oversized file client-side without calling the API', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('No images uploaded yet.')).toBeInTheDocument());

    const tooBig = jpegFile('big.jpg', 11 * 1024 * 1024);
    const user = userEvent.setup();
    await user.upload(screen.getByLabelText('Upload image'), tooBig);

    expect(screen.getByText(/exceeds the maximum allowed size/)).toBeInTheDocument();
    expect(mockUpload).not.toHaveBeenCalled();
  });

  it('rejects an unsupported file type client-side without calling the API', async () => {
    mockFetch.mockResolvedValue([]);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getByText('No images uploaded yet.')).toBeInTheDocument());

    const pdf = new File(['data'], 'doc.pdf', { type: 'application/pdf' });
    const user = userEvent.setup({ applyAccept: false });
    await user.upload(screen.getByLabelText('Upload image'), pdf);

    expect(screen.getByText(/Unsupported file type/)).toBeInTheDocument();
    expect(mockUpload).not.toHaveBeenCalled();
  });

  it('reorders images when moving one later', async () => {
    mockFetch.mockResolvedValue([imgA, imgB]);
    mockReorder.mockResolvedValue([
      { ...imgB, displayOrder: 0 },
      { ...imgA, displayOrder: 1 },
    ]);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(2));

    const user = userEvent.setup();
    await user.click(screen.getAllByLabelText('Move later')[0]);

    await waitFor(() => expect(mockReorder).toHaveBeenCalledWith(1, [11, 10]));
  });

  it('deletes an image after confirmation', async () => {
    mockFetch.mockResolvedValue([imgA, imgB]);
    mockDelete.mockResolvedValue(undefined);

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(2));

    const user = userEvent.setup();
    await user.click(screen.getAllByText('Delete')[0]);

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(1, 10));
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(1));
  });

  it('shows an error and keeps the image when delete fails', async () => {
    mockFetch.mockResolvedValue([imgA]);
    mockDelete.mockRejectedValue(new Error('Failed to delete image'));

    render(<ProductImagesModal product={product} onClose={vi.fn()} />);
    await waitFor(() => expect(screen.getAllByRole('img')).toHaveLength(1));

    const user = userEvent.setup();
    await user.click(screen.getByText('Delete'));

    await waitFor(() => expect(screen.getByText('Failed to delete image')).toBeInTheDocument());
    expect(screen.getAllByRole('img')).toHaveLength(1);
  });
});
