interface Props {
  message: string;
  onRetry?: () => void;
}

export function ErrorMessage({ message, onRetry }: Props) {
  return (
    <div className="flex flex-col items-center justify-center py-24 text-center">
      <span className="text-5xl mb-4">⚠️</span>
      <p className="text-lg font-semibold text-gray-800">Something went wrong</p>
      <p className="text-sm text-gray-500 mt-1 max-w-sm">{message}</p>
      {onRetry && (
        <button
          onClick={onRetry}
          className="mt-4 px-4 py-2 text-sm font-medium text-white bg-primary-600 rounded-lg hover:bg-primary-700 transition-colors"
        >
          Try again
        </button>
      )}
    </div>
  );
}
