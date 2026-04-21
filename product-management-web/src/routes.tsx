import App from './App';

export const getAppRoutes = () => [
  {
    path: '/',
    element: <App />
  },
  {
    path: '*',
    element: <App />
  }
];
