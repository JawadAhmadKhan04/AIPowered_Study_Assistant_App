module.exports = {
  root: true,
  env: {
    es6: true,
    node: true,
  },
  parserOptions: {
    "ecmaVersion": 2018,
  },
  extends: [
    "eslint:recommended",
  ],
  rules: {
    // Disable all rules
    "quotes": "off",
    "indent": "off",
    "comma-dangle": "off",
    "max-len": "off",
    "no-trailing-spaces": "off",
    "object-curly-spacing": "off",
    "no-multi-spaces": "off",
    // Add any other rules you want to disable
  },
  overrides: [
    {
      files: ["**/*.spec.*"],
      env: {
        mocha: true,
      },
      rules: {},
    },
  ],
  globals: {},
};
