/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        base: '#FFFFFF',
        panel: '#F5E6D8',
        surface: '#F0E4D7',
        charcoal: '#2B2320',
        muted: '#8A7F76',
        accent: {
          blue: '#4285F4',
          red: '#EA4335',
          yellow: '#FBBC05',
          green: '#34A853',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
      },
      boxShadow: {
        soft: '0 2px 10px rgba(43, 35, 32, 0.04)',
        card: '0 4px 20px rgba(43, 35, 32, 0.06)',
      }
    },
  },
  plugins: [],
}
