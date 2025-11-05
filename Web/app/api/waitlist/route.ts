import { NextResponse } from "next/server";

type WaitlistPayload = {
  name: string;
  email: string;
  phone?: string;
  message?: string;
};

const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const sanitizeValue = (value?: string) => value?.trim() ?? "";

const forwardToWebhook = async (payload: WaitlistPayload) => {
  const webhookUrl = process.env.WAITLIST_WEBHOOK_URL;

  if (!webhookUrl) {
    return;
  }

  const response = await fetch(webhookUrl, {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      ...payload,
      submittedAt: new Date().toISOString(),
      source: "web-waitlist"
    })
  });

  if (!response.ok) {
    throw new Error(`Webhook responded with status ${response.status}`);
  }
};

export async function POST(request: Request) {
  try {
    const body = await request.json();
    const name = sanitizeValue(body?.name);
    const email = sanitizeValue(body?.email).toLowerCase();
    const phone = sanitizeValue(body?.phone);
    const message = sanitizeValue(body?.message);

    if (!name || !email) {
      return NextResponse.json(
        { error: "Por favor, completa tu nombre y correo electrónico." },
        { status: 400 }
      );
    }

    if (!emailPattern.test(email)) {
      return NextResponse.json(
        { error: "El correo electrónico no tiene un formato válido." },
        { status: 400 }
      );
    }

    const payload: WaitlistPayload = {
      name,
      email,
      phone: phone || undefined,
      message: message || undefined
    };

    try {
      await forwardToWebhook(payload);
    } catch (error) {
      console.error("Failed to forward waitlist submission", error);
      return NextResponse.json(
        {
          error:
            "Guardamos tu interés pero no pudimos contactar con el equipo automáticamente. Escríbenos a hola@ezivia.com."
        },
        { status: 502 }
      );
    }

    return NextResponse.json({ ok: true });
  } catch (error) {
    return NextResponse.json(
      { error: "No pudimos procesar la solicitud. Revisa los datos e inténtalo de nuevo." },
      { status: 400 }
    );
  }
}
