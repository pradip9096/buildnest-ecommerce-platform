import type { SortOption } from '../../types';

const SORT_OPTIONS: { value: SortOption; label: string }[] = [
  { value: 'relevance', label: 'Relevance' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'newest', label: 'Newest First' },
];

interface Props {
  value: SortOption;
  onChange: (v: SortOption) => void;
}

export function SortDropdown({ value, onChange }: Props) {
  return (
    <div className="flex items-center gap-2">
      <label htmlFor="sort" className="text-sm text-gray-600 whitespace-nowrap">
        Sort by:
      </label>
      <select
        id="sort"
        value={value}
        onChange={e => onChange(e.target.value as SortOption)}
        className="text-sm border border-gray-300 rounded-lg px-3 py-1.5 text-gray-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 bg-white"
      >
        {SORT_OPTIONS.map(opt => (
          <option key={opt.value} value={opt.value}>
            {opt.label}
          </option>
        ))}
      </select>
    </div>
  );
}
