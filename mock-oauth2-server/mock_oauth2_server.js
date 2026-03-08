const express = require('express');
const app = express();
const port = 3000;

app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// --- Mock Database / Config ---
const MOCK_CLIENT_ID = "my-client-id";
const MOCK_CLIENT_SECRET = "my-secret-key";
const VALID_TOKEN = "1aac898b-8c4f-488b-95ae-0961820e0ce2";

/**
 * 1. Auth Server Endpoint (/oauth/token)
 * จำลองการรับ Client ID/Secret เพื่อออก Token
 */
app.post('/oauth/token', (req, res) => {
    const { client_id, client_secret, grant_type } = req.body;

    // ตรวจสอบว่าเป็นการขอแบบ client_credentials หรือไม่
    if (grant_type !== 'client_credentials') {
        return res.status(400).json({ error: "unsupported_grant_type" });
    }

    // ตรวจสอบความถูกต้องของ Credentials
    if (client_id === MOCK_CLIENT_ID && client_secret === MOCK_CLIENT_SECRET) {
        console.log("ออก Token ใหม่ให้ Client");

        const expiresIn = 3600;
        const expiresAt = Math.floor(Date.now() / 1000) + expiresIn;

        return res.json({
            status: {
                code: 1000,
                description: "Success"
            },
            data: {
                accessToken: "1aac898b-8c4f-488b-95ae-0961820e0ce2", // หรือ VALID_TOKEN
                tokenType: "Bearer",
                expiresIn: expiresIn,
                expiresAt: expiresAt
            }
        });
    } else {
        return res.status(401).json({ error: "invalid_client" });
    }
});

/**
 * 2. Resource Server Endpoint (/api/resource)
 * จำลอง API ปลายทางที่รอรับ Request ที่ถูก "Inject Token" มาแล้ว
 */
app.get('/api/resource', (req, res) => {
    const authHeader = req.headers['authorization'];

    console.log(`ได้รับ Request ที่ Header: ${authHeader}`);

    if (authHeader === `Bearer ${VALID_TOKEN}`) {
        res.json({
            status: "success",
            message: "success",
            data: { id: 1, name: "Product information" }
        });
    } else {
        console.log("Token ไม่ถูกต้อง หรือไม่ได้ใส่มา");
        res.status(403).json({
            status: "error",
            message: "Access Denied: Invalid or Missing Token"
        });
    }
});

app.listen(port, () => {
    console.log(`
Mock OAuth2 Server รันอยู่ที่ http://localhost:${port}
--------------------------------------------------
1. ขอ Token: POST http://localhost:${port}/oauth/token
   Body: client_id=${MOCK_CLIENT_ID}&client_secret=${MOCK_CLIENT_SECRET}&grant_type=client_credentials

2. เข้าถึงข้อมูล: GET http://localhost:${port}/api/resource
   Header: Authorization: Bearer ${VALID_TOKEN}
--------------------------------------------------
    `);
});