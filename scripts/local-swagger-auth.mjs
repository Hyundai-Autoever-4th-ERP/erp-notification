import { generateKeyPairSync, sign } from 'node:crypto';
import { createServer } from 'node:http';

const host = '127.0.0.1';
const port = 18086;
const path = '/internal/auth/jwks';
const { publicKey, privateKey } = generateKeyPairSync('rsa', { modulusLength: 2048 });
const publicJwk = { ...publicKey.export({ format: 'jwk' }), kid: 'local-swagger', alg: 'RS256', use: 'sig' };

function encode(value) {
    return Buffer.from(JSON.stringify(value)).toString('base64url');
}

function issueToken(subject, role) {
    const now = Math.floor(Date.now() / 1000);
    const header = encode({ alg: 'RS256', kid: publicJwk.kid, typ: 'JWT' });
    const claims = encode({ sub: subject, role, iat: now, exp: now + 3600 });
    const payload = header + '.' + claims;
    return payload + '.' + sign('RSA-SHA256', Buffer.from(payload), privateKey).toString('base64url');
}

const server = createServer((request, response) => {
    if (request.method !== 'GET' || request.url !== path) {
        response.writeHead(404).end();
        return;
    }
    response.writeHead(200, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify({ keys: [publicJwk] }));
});

server.listen(port, host, () => {
    console.log('로컬 테스트용 JWKS http://' + host + ':' + port + path);
    console.log('Swagger Authorize에 사용할 토큰 (1시간 유효)');
    for (const [subject, role] of [['DEMO-001', 'BRANCH_STAFF'], ['DEMO-ADMIN', 'ADMIN'], ['local-sales', 'NOTIFICATION_PUBLISHER']]) {
        console.log(role + '\n' + issueToken(subject, role));
    }
});
