interface Props {
  page: number;
  totalPages: number;
  onPageChange: (p: number) => void;
}

export function Pagination({ page, totalPages, onPageChange }: Props) {
  if (totalPages <= 1) return null;

  const pages = buildPageRange(page, totalPages);

  return (
    <nav aria-label="Pagination" className="flex items-center justify-center gap-1 mt-8">
      <button
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
        aria-label="Previous page"
      >
        ‹
      </button>

      {pages.map((p, i) =>
        p === -1 ? (
          <span key={`ellipsis-${i}`} className="px-2 text-gray-400">…</span>
        ) : (
          <button
            key={p}
            onClick={() => onPageChange(p)}
            aria-current={p === page ? 'page' : undefined}
            className={`px-3 py-1.5 text-sm rounded-lg border ${
              p === page
                ? 'bg-primary-600 border-primary-600 text-white font-medium'
                : 'border-gray-300 text-gray-700 hover:bg-gray-50'
            }`}
          >
            {p + 1}
          </button>
        )
      )}

      <button
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        className="px-3 py-1.5 text-sm rounded-lg border border-gray-300 text-gray-600 hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed"
        aria-label="Next page"
      >
        ›
      </button>
    </nav>
  );
}

function buildPageRange(current: number, total: number): number[] {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i);
  const pages: number[] = [0];
  if (current > 2) pages.push(-1);
  for (let i = Math.max(1, current - 1); i <= Math.min(total - 2, current + 1); i++) {
    pages.push(i);
  }
  if (current < total - 3) pages.push(-1);
  pages.push(total - 1);
  return pages;
}
