import { Linkedin, Github, Mail, Globe } from "lucide-react";

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-white py-6">
      <div className="max-w-6xl mx-auto px-4 flex flex-col md:flex-row items-center justify-between gap-4">
        
        {/* Left side - copyright */}
        <p className="text-sm text-gray-400">
          © {new Date().getFullYear()} Michael Sim. All rights reserved.
        </p>

        {/* Right side - social + website links */}
        <div className="flex gap-6 text-xl items-center">
          <a
            href="https://www.linkedin.com/in/michaeldavidsim/"
            className="hover:text-blue-400 transition-colors"
          >
            <Linkedin size={20} />
          </a>

          <a
            href="https://github.com/michaeldsim"
            className="hover:text-gray-400 transition-colors"
          >
            <Github size={20} />
          </a>

          <a
            href="mailto:michael.sim132@gmail.com"
            className="hover:text-red-400 transition-colors"
          >
            <Mail size={20} />
          </a>

          {/* Website link with globe icon */}
          <a
            href="https://michaeldavidsim.com/"
            className="hover:text-green-400 flex items-center gap-1 transition-colors"
          >
            <Globe size={20} />
            <span className="text-sm">https://michaeldavidsim.com/</span>
          </a>
        </div>
      </div>
    </footer>
  );
}
