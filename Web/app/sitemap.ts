import type { MetadataRoute } from "next";

const baseUrl = "https://www.eazivi.com";

export default function sitemap(): MetadataRoute.Sitemap {
  const routes: Array<MetadataRoute.Sitemap[number]> = [
    {
      url: baseUrl,
      lastModified: new Date()
    },
    {
      url: `${baseUrl}/faq`,
      lastModified: new Date()
    },
    {
      url: `${baseUrl}/cuidadores`,
      lastModified: new Date()
    },
    {
      url: `${baseUrl}/legal`,
      lastModified: new Date()
    }
  ];

  return routes;
}
