import { useState, type FormEvent } from 'react';
import { Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Alert, Box, Button, Container, Paper, TextField, Typography } from '@mui/material';
import { useAuth } from './AuthContext';

interface LocationState {
  from?: { pathname: string };
}

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const redirectTo = (location.state as LocationState | null)?.from?.pathname ?? '/';

  if (isAuthenticated) {
    return <Navigate to={redirectTo} replace />;
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(username, password);
      navigate(redirectTo, { replace: true });
    } catch {
      setError('Usuario o contraseña incorrectos');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Container maxWidth="xs">
      <Box sx={{ mt: 8 }}>
        <Paper sx={{ p: 4 }}>
          <Typography variant="h5" gutterBottom>
            Iniciar sesión
          </Typography>
          <form onSubmit={handleSubmit}>
            <TextField
              label="Usuario"
              fullWidth
              margin="normal"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              autoFocus
            />
            <TextField
              label="Contraseña"
              type="password"
              fullWidth
              margin="normal"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {error && (
              <Alert severity="error" sx={{ mt: 2 }}>
                {error}
              </Alert>
            )}
            <Button type="submit" variant="contained" fullWidth sx={{ mt: 3 }} disabled={submitting}>
              {submitting ? 'Ingresando...' : 'Ingresar'}
            </Button>
          </form>
        </Paper>
      </Box>
    </Container>
  );
}
