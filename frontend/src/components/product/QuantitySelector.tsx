interface Props {
  value: number;
  min?: number;
  max: number;
  onChange: (value: number) => void;
}

export function QuantitySelector({ value, min = 1, max, onChange }: Props) {
  return (
    <div className="flex items-center gap-0">
      <button
        onClick={() => onChange(Math.max(min, value - 1))}
        disabled={value <= min}
        className="w-10 h-10 rounded-l-lg border border-gray-300 bg-gray-50 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed text-lg font-bold flex items-center justify-center"
        aria-label="Decrease quantity"
      >
        −
      </button>
      <span className="w-12 h-10 border-t border-b border-gray-300 flex items-center justify-center text-sm font-semibold tabular-nums">
        {value}
      </span>
      <button
        onClick={() => onChange(Math.min(max, value + 1))}
        disabled={value >= max}
        className="w-10 h-10 rounded-r-lg border border-gray-300 bg-gray-50 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed text-lg font-bold flex items-center justify-center"
        aria-label="Increase quantity"
      >
        +
      </button>
    </div>
  );
}
