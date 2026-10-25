import { createApp } from "./app";
import { loadConfig } from "./config/env";

const config = loadConfig();
const app = createApp({ config });

app.listen(config.port, "0.0.0.0", () => {
  console.info(`Product catalogue API listening on port ${config.port}`);
});
