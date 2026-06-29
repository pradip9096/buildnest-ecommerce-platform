interface Props {
  rating: number;
  max?: number;
  size?: 'sm' | 'md' | 'lg';
}

export function StarRating({ rating, max = 5, size = 'md' }: Props) {
  const sizeClass = size === 'sm' ? 'text-sm' : size === 'lg' ? 'text-2xl' : 'text-base';

  return (
    <span className={`inline-flex gap-0.5 ${sizeClass}`} aria-label={`${rating} out of ${max} stars`}>
      {Array.from({ length: max }, (_, i) => {
        const filled = i + 1 <= Math.floor(rating);
        const half = !filled && i + 0.5 < rating;
        return (
          <span key={i} className={filled || half ? 'text-amber-400' : 'text-gray-300'}>
            {filled ? '★' : half ? '½' : '☆'}
          </span>
        );
      })}
    </span>
  );
}
