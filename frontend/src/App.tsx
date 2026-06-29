import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { ProductListingPage } from './pages/ProductListingPage';

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<ProductListingPage />} />
      </Routes>
    </BrowserRouter>
  );
}
