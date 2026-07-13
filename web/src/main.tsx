import { StrictMode } from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter, useRoutes } from 'react-router-dom';
import { ThemeProvider, createTheme, CssBaseline } from '@mui/material';
import { getAppRoutes } from './routes';
import { AuthProvider } from './auth/AuthContext';

const theme = createTheme({
  palette: {
    primary: { main: '#1976d2' },
    secondary: { main: '#9c27b0' },
  },
  typography: {
    fontFamily: 'Arial, sans-serif',
  },
});

function RoutesContainer() {
  const routes = getAppRoutes();
  return useRoutes(routes);
}

function AppRoot() {
  return (
    <StrictMode>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <AuthProvider>
          <BrowserRouter>
            <RoutesContainer />
          </BrowserRouter>
        </AuthProvider>
      </ThemeProvider>
    </StrictMode>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(<AppRoot />);