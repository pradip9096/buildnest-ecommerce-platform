const STEPS = ['Address', 'Shipping', 'Payment', 'Confirm'];

interface Props {
  currentStep: number;
}

export function CheckoutStepper({ currentStep }: Props) {
  return (
    <nav aria-label="Checkout progress" className="flex items-center justify-center gap-0 mb-8">
      {STEPS.map((label, idx) => {
        const done = idx < currentStep;
        const active = idx === currentStep;
        return (
          <div key={label} className="flex items-center">
            <div className="flex flex-col items-center">
              <div
                className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold border-2 transition-colors ${
                  done
                    ? 'bg-green-500 border-green-500 text-white'
                    : active
                    ? 'bg-primary-500 border-primary-500 text-white'
                    : 'bg-white border-gray-300 text-gray-400'
                }`}
              >
                {done ? '✓' : idx + 1}
              </div>
              <span
                className={`mt-1 text-xs font-medium ${
                  active ? 'text-primary-600' : done ? 'text-green-600' : 'text-gray-400'
                }`}
              >
                {label}
              </span>
            </div>
            {idx < STEPS.length - 1 && (
              <div
                className={`h-0.5 w-16 sm:w-24 mx-1 mb-4 transition-colors ${
                  done ? 'bg-green-400' : 'bg-gray-200'
                }`}
              />
            )}
          </div>
        );
      })}
    </nav>
  );
}
