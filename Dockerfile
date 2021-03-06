FROM node:lts-alpine
# app directory
WORKDIR /usr/src/app
# Copy neccessary files
COPY ./functions ./functions
COPY ./firebase.json ./
COPY ./.firebaserc ./
WORKDIR /usr/src/app/functions
RUN npm i --only=production -g pm2@latest firebase-tools cross-env typescript
RUN npm ci --only=production
CMD ["pm2-runtime", "start", "daemon.json"]
