double cosCurve(double number:
	number ^= 2
	(1.0L - 16.0L * number) * (1.0L - (16.0L - 4.0L * pi) * number)
)

double approxSin(double angle:
	angle *= 1.0L / tau
	angle -= floor(angle)
	angle <= 0.5L ? cosCurve(angle - 0.25L) : -cosCurve(angle - 0.75L)
)

double approxCos(double angle:
	approxSin(angle + pi * 0.5L)
)