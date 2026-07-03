import type { ReactNode } from 'react';
import { AppBar, Box, Button, Toolbar, Typography } from '@mui/material';
import { useAuth } from './AuthContext';

export default function AppLayout({ children }: { children: ReactNode }) {
  const { username, logout } = useAuth();

  return (
    <Box>
      <AppBar position="static">
        <Toolbar sx={{ justifyContent: 'space-between' }}>
          <Typography variant="h6">Product Management</Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            {username && <Typography variant="body2">{username}</Typography>}
            <Button color="inherit" onClick={logout}>
              Cerrar sesión
            </Button>
          </Box>
        </Toolbar>
      </AppBar>
      {children}
    </Box>
  );
}
