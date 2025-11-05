import { createBrowserRouter } from 'react-router-dom';
import App from './routes/App';
import Attractions from './routes/Attractions';
import NotFound from './routes/NotFound';

const router = createBrowserRouter(
  [
    {
      path: '/',
      element: <App />,
      children: [
        {
          index: true,
          lazy: async () => ({
            Component: (await import('./routes/Home')).default
          })
        },
        {
          path: 'agenda',
          element: <Attractions />
        }
      ]
    },
    {
      path: '*',
      element: <NotFound />
    }
  ],
  {
    basename: '/WEB2'
  }
);

export default router;
