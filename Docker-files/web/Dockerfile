FROM nginx

LABEL org.opencontainers.image.title="VProfile web proxy" \
      org.opencontainers.image.description="Nginx reverse proxy for the VProfile application"

RUN rm -f /etc/nginx/conf.d/default.conf

COPY nginvproapp.conf /etc/nginx/conf.d/vproapp.conf
