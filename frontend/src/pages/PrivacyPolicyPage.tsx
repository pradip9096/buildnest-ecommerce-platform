import { Link } from 'react-router-dom';
import { SeoMeta } from '../components/common/SeoMeta';

/**
 * GDPR compliance (#128, COMP-01) — linked from RegisterPage's mandatory
 * consent checkbox. Static content; no data collection on this page itself.
 */
export function PrivacyPolicyPage() {
  return (
    <div className="min-h-screen bg-gray-50 px-4 py-10">
      <SeoMeta title="Privacy Policy" description="Read BuildNest's privacy policy to learn how we collect, use, and protect your personal information." />
      <div className="max-w-2xl mx-auto bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Privacy Policy</h1>

        <section className="space-y-4 text-sm text-gray-700 leading-relaxed">
          <p>
            BuildNest collects the personal information you provide at registration
            (name, username, email, phone number) and generated while you use the
            platform (orders, addresses, reviews, wishlist, cart) to operate your
            account and fulfil your orders.
          </p>
          <h2 className="text-lg font-semibold text-gray-900 pt-2">Your rights</h2>
          <p>
            You can download a copy of all data associated with your account from{' '}
            <Link to="/account" className="text-primary-600 hover:text-primary-700 font-medium">
              your account page
            </Link>{' '}
            at any time (right to access).
          </p>
          <p>
            You can request deletion of your account from the same page. Your
            account is deactivated immediately; your personal information is
            permanently anonymised 30 days later (right to erasure). Order and
            financial records are retained beyond that point for legal/tax
            purposes, but are no longer linked to your identifying information.
          </p>
          <h2 className="text-lg font-semibold text-gray-900 pt-2">Contact</h2>
          <p>
            For any privacy-related questions, contact support through the
            channels listed on our homepage.
          </p>
        </section>
      </div>
    </div>
  );
}
