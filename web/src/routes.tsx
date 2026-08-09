import App from './App';
import LoginPage from './auth/LoginPage';
import RequireAuth from './auth/RequireAuth';
import AppLayout from './auth/AppLayout';

const protectedElement = (
  <RequireAuth>
    <AppLayout>
      <App />
    </AppLayout>
  </RequireAuth>
);

export const getAppRoutes = () => [
  {
    path: '/login',
    element: <LoginPage />
  },
  {
    path: '/',
    element: protectedElement
  },
  {
    path: '*',
    element: protectedElement
  }
];
