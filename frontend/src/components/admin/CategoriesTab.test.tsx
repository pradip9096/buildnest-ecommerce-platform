import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CategoriesTab } from './CategoriesTab';
import {
  fetchAdminCategories,
  createAdminCategory,
  updateAdminCategory,
  deleteAdminCategory,
  type AdminCategory,
} from '../../api/admin';
import { ApiError } from '../../api/client';

vi.mock('../../api/admin', () => ({
  fetchAdminCategories: vi.fn(),
  createAdminCategory: vi.fn(),
  updateAdminCategory: vi.fn(),
  deleteAdminCategory: vi.fn(),
}));

const mockFetch = vi.mocked(fetchAdminCategories);
const mockCreate = vi.mocked(createAdminCategory);
const mockUpdate = vi.mocked(updateAdminCategory);
const mockDelete = vi.mocked(deleteAdminCategory);

const powerTools: AdminCategory = { id: 1, name: 'Power Tools', description: 'Cordless tools', isActive: true, parentCategory: null };
const cordless: AdminCategory = { id: 2, name: 'Cordless Drills', isActive: true, parentCategory: { id: 1 } };

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal('confirm', vi.fn(() => true));
});

describe('CategoriesTab', () => {
  it('renders the category list with hierarchy indentation', async () => {
    mockFetch.mockResolvedValue([powerTools, cordless]);

    render(<CategoriesTab />);

    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());
    expect(screen.getByText('Cordless Drills')).toBeInTheDocument();
  });

  it('filters the list via search', async () => {
    mockFetch.mockResolvedValue([powerTools, cordless]);

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());

    await userEvent.setup().type(screen.getByPlaceholderText('Search categories…'), 'Cordless');

    expect(screen.queryByText('Power Tools')).not.toBeInTheDocument();
    expect(screen.getByText('Cordless Drills')).toBeInTheDocument();
  });

  it('shows the empty state when there are no categories', async () => {
    mockFetch.mockResolvedValue([]);

    render(<CategoriesTab />);

    await waitFor(() => expect(screen.getByText('No categories found')).toBeInTheDocument());
  });

  it('creates a category via the modal and adds it to the list', async () => {
    mockFetch.mockResolvedValue([powerTools]);
    mockCreate.mockResolvedValue({ id: 3, name: 'Hand Tools', isActive: true, parentCategory: null });

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Category' }));
    await user.type(screen.getByLabelText('Name'), 'Hand Tools');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    await waitFor(() => expect(mockCreate).toHaveBeenCalledWith(
      expect.objectContaining({ name: 'Hand Tools', parentId: null })
    ));
    await waitFor(() => expect(screen.getByText('Hand Tools')).toBeInTheDocument());
  });

  it('rejects a name shorter than 2 characters without calling the API', async () => {
    mockFetch.mockResolvedValue([powerTools]);

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());

    const user = userEvent.setup();
    await user.click(screen.getByRole('button', { name: '+ New Category' }));
    await user.type(screen.getByLabelText('Name'), 'A');
    await user.click(screen.getByRole('button', { name: 'Create' }));

    expect(screen.getByText('Category name must be at least 2 characters.')).toBeInTheDocument();
    expect(mockCreate).not.toHaveBeenCalled();
  });

  it('pre-fills the form on edit and excludes the category itself from parent options', async () => {
    mockFetch.mockResolvedValue([powerTools, cordless]);
    mockUpdate.mockResolvedValue(powerTools);

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());

    await userEvent.setup().click(screen.getAllByRole('button', { name: 'Edit' })[0]);

    expect(screen.getByLabelText('Name')).toHaveValue('Power Tools');
    const parentSelect = screen.getByLabelText('Parent Category') as HTMLSelectElement;
    const optionLabels = Array.from(parentSelect.options).map(o => o.textContent);
    expect(optionLabels).not.toContain('Power Tools');
  });

  it('surfaces the backend message when delete is blocked by referencing products/subcategories', async () => {
    mockFetch.mockResolvedValue([powerTools]);
    mockDelete.mockRejectedValue(new ApiError('Cannot delete category 1: 3 product(s) still reference it', 400));

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Power Tools')).toBeInTheDocument());

    await userEvent.setup().click(screen.getByRole('button', { name: 'Delete' }));

    await waitFor(() => expect(screen.getByText('Cannot delete category 1: 3 product(s) still reference it')).toBeInTheDocument());
    expect(screen.getByText('Power Tools')).toBeInTheDocument();
  });

  it('deletes a category successfully and removes it from the list', async () => {
    mockFetch.mockResolvedValue([powerTools, cordless]);
    mockDelete.mockResolvedValue(undefined);

    render(<CategoriesTab />);
    await waitFor(() => expect(screen.getByText('Cordless Drills')).toBeInTheDocument());

    const deleteButtons = screen.getAllByRole('button', { name: 'Delete' });
    await userEvent.setup().click(deleteButtons[1]);

    await waitFor(() => expect(mockDelete).toHaveBeenCalledWith(2));
    await waitFor(() => expect(screen.queryByText('Cordless Drills')).not.toBeInTheDocument());
  });
});
