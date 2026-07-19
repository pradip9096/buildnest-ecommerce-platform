import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TagsTab } from './TagsTab';
import {
  fetchAdminTags,
  createAdminTag,
  updateAdminTag,
  deleteAdminTag,
  type AdminTag,
} from '../../api/admin';
import { ApiError } from '../../api/client';

vi.mock('../../api/admin', () => ({
  fetchAdminTags: vi.fn(),
  createAdminTag: vi.fn(),
  updateAdminTag: vi.fn(),
  deleteAdminTag: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminTags);
const mockCreate = vi.mocked(createAdminTag);
const mockUpdate = vi.mocked(updateAdminTag);
const mockDelete = vi.mocked(deleteAdminTag);

const ecoFriendly: AdminTag = { id: 1, name: 'Eco-Friendly', slug: 'eco-friendly' };
const bestSeller: AdminTag = { id: 2, name: 'Best Seller', slug: 'best-seller' };

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('TagsTab', () => {
  it('renders the tag list', async () => {
    mockFetch.mockResolvedValue([ecoFriendly, bestSeller]);

    render(<TagsTab />);

    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());
    expect(screen.getByText('Best Seller')).toBeInTheDocument();
    expect(screen.getByText('eco-friendly')).toBeInTheDocument();
  });

  it('filters the list via search', async () => {
    mockFetch.mockResolvedValue([ecoFriendly, bestSeller]);

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search tags…'), 'Best');

    expect(screen.queryByText('Eco-Friendly')).not.toBeInTheDocument();
    expect(screen.getByText('Best Seller')).toBeInTheDocument();
  });

  it('shows the empty state when there are no tags', async () => {
    mockFetch.mockResolvedValue([]);

    render(<TagsTab />);

    await waitFor(() => expect(screen.getByText('No tags found')).toBeInTheDocument());
  });

  it('creates a tag via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([ecoFriendly]);
    mockCreate.mockResolvedValue({ id: 3, name: 'Waterproof', slug: 'waterproof' });

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Tag' }));
    await user.type(screen.getByLabelText('Name'), 'Waterproof');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith({ name: 'Waterproof' }));
    await waitFor(() => expect(screen.getByText('Waterproof')).toBeInTheDocument());
  });

  it('rejects a name shorter than 2 characters without calling the API', async () => {
    mockFetch.mockResolvedValue([ecoFriendly]);

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Tag' }));
    await user.type(screen.getByLabelText('Name'), 'A');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Tag name must be at least 2 characters.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('pre-fills the form on edit', async () => {
    mockFetch.mockResolvedValue([ecoFriendly]);
    mockUpdate.mockResolvedValue(ecoFriendly);

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Edit' }));

    expect(screen.getByLabelText('Name')).toHaveValue('Eco-Friendly');
  });

  it('surfaces the backend error message when create fails (e.g. duplicate name)', async () => {
    mockFetch.mockResolvedValue([ecoFriendly]);
    mockCreate.mockRejectedValue(new ApiError('Tag already exists with name: Eco-Friendly', 400));

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Tag' }));
    await user.type(screen.getByLabelText('Name'), 'Eco-Friendly');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(screen.getByText('Tag already exists with name: Eco-Friendly')).toBeInTheDocument());
  });

  it('surfaces the backend error message when delete fails', async () => {
    mockFetch.mockResolvedValue([ecoFriendly]);
    mockDelete.mockRejectedValue(new ApiError('Tag not found', 404));

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Eco-Friendly')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Delete' }));

    await waitFor(() => expect(screen.getByText('Tag not found')).toBeInTheDocument());
    expect(screen.getByText('Eco-Friendly')).toBeInTheDocument();
  });

  it('deletes a tag successfully and removes it from the list', async () => {
    mockFetch.mockResolvedValue([ecoFriendly, bestSeller]);
    mockDelete.mockResolvedValue(undefined);

    render(<TagsTab />);
    await waitFor(() => expect(screen.getByText('Best Seller')).toBeInTheDocument());

    const deleteButtons = screen.getAllByRole('button', { name: 'Delete' });
    await userEvent.setup().click(deleteButtons[1]);

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(2));
    await waitFor(() => expect(screen.queryByText('Best Seller')).not.toBeInTheDocument());
  });
});
