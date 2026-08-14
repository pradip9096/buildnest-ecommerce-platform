import { Helmet } from 'react-helmet-async';

const SITE_NAME = 'BuildNest';
const DEFAULT_IMAGE = '/favicon.svg';

type SeoMetaProps = {
  title: string;
  description: string;
  image?: string;
  type?: 'website' | 'product';
  url?: string;
};

export function SeoMeta({ title, description, image = DEFAULT_IMAGE, type = 'website', url }: SeoMetaProps) {
  const fullTitle = `${title} | ${SITE_NAME}`;
  const canonicalUrl = url ?? (typeof window !== 'undefined' ? window.location.href : undefined);

  return (
    <Helmet>
      <title>{fullTitle}</title>
      <meta name="description" content={description} />
      {canonicalUrl && <link rel="canonical" href={canonicalUrl} />}

      <meta property="og:title" content={fullTitle} />
      <meta property="og:description" content={description} />
      <meta property="og:type" content={type} />
      <meta property="og:site_name" content={SITE_NAME} />
      <meta property="og:image" content={image} />
      {canonicalUrl && <meta property="og:url" content={canonicalUrl} />}

      <meta name="twitter:card" content="summary_large_image" />
      <meta name="twitter:title" content={fullTitle} />
      <meta name="twitter:description" content={description} />
      <meta name="twitter:image" content={image} />
    </Helmet>
  );
}
