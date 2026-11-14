export interface LoginResult {
  token: string;
  tokenType: string;
  expiresIn: number;
  username: string;
  roles: string[];
}

export async function login(username: string, password: string): Promise<LoginResult> {
  const response = await fetch('/api/v1/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
  });

  if (!response.ok) {
    throw new Error('Usuario o contraseña incorrectos');
  }

  const json = await response.json();
  return json.data as LoginResult;
}
