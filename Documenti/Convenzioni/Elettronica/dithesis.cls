%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% sapthesis.cls                                                   %%
%% version 3.1.1                                                   %%
%% 2012-03-02                                                      %%
%% author: Francesco Biccari                                       %%
%% contact: biccari@gmail.com                                      %%
%%                                                                 %%
%% This file is the LaTeX class for the thesis style               %%
%% of the "Sapienza -- University of Rome"                         %%
%%                                                                 %%
%% see README for license information and installation             %%
%% see sapthesis-doc.pdf for usage                                 %%
%% see sapthesis-example.pdf for document examples                 %%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Identification %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{dithesis}%
[2012/08/08 Dipartimento di Informatica - University of Salerno thesis class]



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Options %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\RequirePackage{xkeyval}
\newlength{\SAP@binding}
\newif\ifSAP@nodefaultfont \SAP@nodefaultfontfalse
\newif\ifSAP@romandiff \SAP@romandifffalse
\newif\ifSAP@customlanguage \SAP@customlanguagefalse
\newif\ifSAP@bn \SAP@bnfalse
\newif\ifSAP@PhD \SAP@PhDfalse
\newif\ifSAP@MasterP \SAP@MasterPfalse
\newif\ifSAP@MasterS \SAP@MasterSfalse
\newif\ifSAP@LaM \SAP@LaMfalse
\newif\ifSAP@Lau \SAP@Laufalse
\newif\ifSAP@noexaminfo \SAP@noexaminfofalse
\newcommand{\SAP@customlanguage}{}

\define@key[sap]{}{noexaminfo}[]{\SAP@noexaminfotrue}
\define@key[sap]{}{nodefaultfont}[]{\SAP@nodefaultfonttrue}
\define@key[sap]{}{romandiff}[]{\SAP@romandifftrue}
\define@key[sap]{}{italian}[]{\SAP@customlanguagetrue
  \renewcommand{\SAP@customlanguage}{\SAP@italian}}
\define@key[sap]{}{english}[]{\SAP@customlanguagetrue
  \renewcommand{\SAP@customlanguage}{\SAP@english}}
\define@key[sap]{}{bn}[]{\SAP@bntrue
  \PassOptionsToPackage{monochrome}{color}
  \PassOptionsToPackage{monochrome}{xcolor}}
\define@key[sap]{}{PhD}[]{\SAP@PhDtrue}
\define@key[sap]{}{LaM}[]{\SAP@LaMtrue}
\define@key[sap]{}{Lau}[]{\SAP@Lautrue}
\define@key[sap]{}{MasterP}[]{\SAP@MasterPtrue\SAP@MasterPtrue}
\define@key[sap]{}{MasterS}[]{\SAP@MasterStrue\SAP@MasterStrue}
\define@key[sap]{}{binding}[0pt]{\setlength{\SAP@binding}{#1}}
\define@key[sap]{}{draft}[]{\PassOptionsToClass{draft}{book}}
\define@key[sap]{}{oneside}[]{\PassOptionsToClass{oneside}{book}}
\define@key[sap]{}{twoside}[]{}
\DeclareOptionX*{\ClassError{sapthesis}{Option '\CurrentOption'%
  \MessageBreak is not valid}{Remove '\CurrentOption' %
  from class options}}
\ProcessOptionsX[sap]<>
\LoadClass[a4paper,11pt]{book}


\ifSAP@PhD\relax\else
  \ifSAP@LaM\relax\else
    \ifSAP@Lau\relax\else
      \ifSAP@MasterP\relax\else
        \ifSAP@MasterS\relax\else
          \ClassError{sapthesis}{You must specify the PhD, LaM, Lau, MasterP or%
          MasterS option}{}
        \fi
      \fi
    \fi
  \fi
\fi



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Page layout %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

\RequirePackage[a4paper]{geometry}
\geometry{textwidth=396pt,
  textheight=641pt,
  marginparsep=12pt,
  bindingoffset=\SAP@binding,
  hmarginratio=55:89,
  vmarginratio=55:89,
  includehead,
  marginparwidth=72pt,
  heightrounded}
  
\if@twoside
  \geometry{hmarginratio=55:89}
\else
  \geometry{hmarginratio=1:1}
\fi



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Some settings%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\RequirePackage{ifxetex}
\ifxetex
  \RequirePackage{xltxtra}
\else
  \ifSAP@nodefaultfont\relax\else
    \RequirePackage[T1]{fontenc}
    \RequirePackage{textcomp}
    \RequirePackage{lmodern}
  \fi
\fi
\RequirePackage{caption}
\captionsetup{indention=\parindent,labelfont={small,bf},labelsep=period,%
  tableposition=top,textfont={small}}
\RequirePackage{graphicx}
\RequirePackage{color}
\RequirePackage{booktabs}
\RequirePackage{amsmath}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Fancy headers %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

%% this two lines remove the headers in the empty pages
%% (instead of loading the emptypage package)
\def\cleardoublepage{\clearpage\if@twoside \ifodd\c@page\else
\thispagestyle{empty}\hbox{}\newpage\fi\fi}

\RequirePackage{fancyhdr}

%% definition of the main page style
\fancypagestyle{SAP@mainstyle}{%
\pagestyle{fancy}
\renewcommand{\chaptermark}[1]{\markboth{\ifnum \c@chapter>0 \thechapter.\ ##1 \else ##1 \fi}{}}
\renewcommand{\sectionmark}[1]{\markright{\thesection\ ##1}}
\fancyhf{}
\fancyhead[LE,RO]{\small\bfseries\thepage}
\fancyhead[LO]{\small\bfseries\nouppercase{\rightmark}}
\fancyhead[RE]{\small\bfseries\nouppercase{\leftmark}}
\renewcommand{\headrulewidth}{0.4pt}
\renewcommand{\footrulewidth}{0pt}}

%% ri-definition of the plain style (first page of the chapters)
\fancypagestyle{plain}{%
\renewcommand{\headrulewidth}{0pt}
\fancyhf{}
%\fancyfoot[C]{\small\scshape\thepage}
\fancyhead[LE,RO]{\small\bfseries\thepage}
}

\g@addto@macro\frontmatter{\pagestyle{SAP@mainstyle}}
\g@addto@macro\mainmatter{\pagestyle{SAP@mainstyle}}

% fancy marginnote

% commands to redefine the marginpar command
% maybe in future I will remove the mnote command?
%\def \@marginparreset {%
%        \reset@font
%        \footnotesize
%        \itshape
%        \@setminipage
%}
%\long\def\@xmpar[#1]#2{%
%  \@savemarbox\@marbox{\raggedleft #1}%
%  \@savemarbox\@currbox{\raggedright #2}%
%  \@xympar}
%\long\def\@ympar#1{%
%  \@savemarbox\@marbox{\raggedleft #1}%
%  \@savemarbox\@currbox{\raggedright #1}%
%  \@xympar}
%  
% simpler version which cannot redefine marginpar. A new command
% (mnote) is needed
\g@addto@macro\@marginparreset{\footnotesize\itshape}
\newcommand{\mnote}[1]{\marginpar[\raggedleft #1]{\raggedright #1}}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% numbering objects per chapters (e.g. 1.1, 1.2, ...) %%%%%%%%%%%%%%

\numberwithin{equation}{chapter}
\numberwithin{figure}{chapter}
\numberwithin{table}{chapter}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% redefinition of the \maketitle command to generate the titlepage %

%% GENERAL COMMANDS

\def\SAP@courseorganizerLabel{}
\def\SAP@courseLabel{}
\def\SAP@PhDcourseDeclarationLabel{}

\newcommand{\courseorganizer}[1]{\def\SAP@courseorganizerLabel{#1}}
\newcommand{\course}[2][]{%
  \def\SAP@courseLabel{#2}
  \def\SAP@PhDcourseDeclarationLabel{\if?#1?\SAP@courseLabel\else#1\fi}}

\def\SAP@copyyear{}
\newcommand{\copyyear}[1]{\def\SAP@copyyear{#1}}

\def\SAP@cycle{}
\newcommand{\cycle}[1]{\def\SAP@cycle{#1}}

\def\SAP@submitdate{}
\newcommand{\submitdate}[1]{\def\SAP@submitdate{#1}}

\def\SAP@IDnumber{}
\newcommand{\IDnumber}[1]{\def\SAP@IDnumber{#1}}

\newcount\SAP@advisorcount
\newtoks\SAP@advisortoks
\newcommand{\advisor}[1]{% 
  \ifnum\SAP@advisorcount=\z@ 
    \SAP@advisortoks={#1}% 
  \else 
    \SAP@advisortoks=\expandafter{\the\SAP@advisortoks\\[0.75mm]#1}% 
  \fi 
  \advance\SAP@advisorcount\@ne}

\newcount\SAP@coadvisorcount
\newtoks\SAP@coadvisortoks
\newcommand{\coadvisor}[1]{% 
  \ifnum\SAP@coadvisorcount=\z@ 
    \SAP@coadvisortoks={#1}% 
  \else 
    \SAP@coadvisortoks=\expandafter{\the\SAP@coadvisortoks\\[0.75mm]#1}% 
  \fi 
  \advance\SAP@coadvisorcount\@ne}
  
\newcount\SAP@examinercount
\newtoks\SAP@examinertoks
\newcommand{\examiner}[1]{% 
  \ifnum\SAP@examinercount=\z@ 
    \SAP@examinertoks={#1 (\SAP@chairman)}% 
  \else 
    \SAP@examinertoks=\expandafter{\the\SAP@examinertoks\\[0.75mm]#1}% 
  \fi 
  \advance\SAP@examinercount\@ne}

\def\SAP@examdate{}
\newcommand{\examdate}[1]{\def\SAP@examdate{#1}}

\def\SAP@versiondate{}
\newcommand{\versiondate}[1]{\def\SAP@versiondate{#1}}

\def\SAP@authoremail{}
\newcommand{\authoremail}[1]{\def\SAP@authoremail{#1}}

\def\SAP@website{}
\newcommand{\website}[1]{\def\SAP@website{#1}}

\def\SAP@ISBN{}
\newcommand{\ISBN}[1]{\def\SAP@ISBN{#1}}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% PhD maketitle

\newcommand{\SAP@maketitlePhD}{

% Errors:

\if|\SAP@courseorganizerLabel|\ClassError{sapthesis}{You must specify the Ph.D. %
  organizer}{Example: \protect\courseorganizer{Scuola di dottorato di ...}}\fi
\if|\SAP@courseLabel|\ClassError{sapthesis}{You must specify the Ph.D. %
  course}{Example: \protect\course{Fisica}{Physics}}\fi
\if|\SAP@cycle|\ClassError{sapthesis}{You must specify the Ph.D. %
  cycle (cycle)}{The Ph.D. cycle must be in the roman numbers}\fi
% \if|\SAP@submitdate|\ClassError{sapthesis}{You must specify the %
%  thesis submission date}{The date must be in the form Month(name) Year(number)}\fi
\if|\SAP@copyyear|\ClassError{sapthesis}{You must specify the %
  copyright year}{Example: \protect\copyyear{2012}}\fi
% \if|\SAP@IDnumber|\ClassError{sapthesis}{You must specify your %
%  ID number (matricola)}{E.g. 123456}\fi
\if|\SAP@authoremail|\ClassError{sapthesis}{You must specify the email of the author}{%
  Example: \protect\authoremail{author@email.com}}\fi

% Titlepage:

	\begin{titlepage}

    \newcommand{\SAP@parbox}[2][]{\hspace{1.72cm}\parbox[t][##1][t]{11.9cm}{##2}}
    \parindent=0pt
    \sffamily

    \vspace*{-10mm}
    \begin{center}
    \ifSAP@bn
      \includegraphics[width=5cm]{UniSaBWHiRes.jpg}
    \else
      \includegraphics[width=5cm]{UniSaColor.pdf}
    \fi
    \end{center}
    \nointerlineskip
    \vspace{\stretch{1}}
    \nointerlineskip

    \SAP@parbox{\LARGE\textcolor{sapred}{\@title}\par}

    \vspace{\stretch{1}}

    \SAP@parbox[3cm]{%
    \textcolor{sapred}{\SAP@courseorganizerLabel\\[2mm]
    Technical Report \SAP@courseLabel\ }}

%    \SAP@parbox[3cm]{%
%    \SAP@CandidateLabel\\[2mm]
%    \@author\\[0.75mm]
%    \SAP@IDnumberLabel\ \SAP@IDnumber}

    
    
    
    \SAP@parbox[4.5cm]{%
    \makebox[11.9cm][l]{%
      \parbox[t]{5.9cm}{%
        \ifcase\SAP@advisorcount
          \ClassError{sapthesis}{You must specify at least one advisor in the preamble}%
          {E.g. \protect\advisor{Prof. Name Surname}}
        \or
          \SAP@ThesisAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@advisortoks}
        \hspace{1.5cm}
        \parbox[t]{5.9cm}{%
        \ifcase\SAP@coadvisorcount
          \relax
        \or
          \SAP@ThesisCoAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisCoAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@coadvisortoks}
    }}

%    \SAP@parbox{\SAP@DeclarationLabel\ \SAP@PhDcourseDeclarationLabel\\[2mm]
%        \SAP@parbox{ \SAP@submitdate}
     \SAP@parbox{\SAP@versionLabel\ \SAP@IDnumber\  -- \SAP@versiondate}
    
	\end{titlepage}
\clearpage
\SAP@composebacktitlepage
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Laurea and Laurea Magistrale maketitle

\newcommand{\SAP@maketitleLaurea}{

% Errors:

\if|\SAP@courseorganizerLabel|\ClassError{sapthesis}{You must specify the degree %
  organizer}{Example: \protect\courseorganizer{Faculty of ...}}\fi
% \if|\SAP@submitdate|\ClassError{sapthesis}{You must specify the Academic %
% Year}{Example: \protect\submitdate{2011/2012}}\fi
%\if|\SAP@IDnumber|\ClassError{sapthesis}{You must specify your %
%  ID number (matricola)}{E.g. 123456}\fi
\if|\SAP@copyyear|\ClassError{sapthesis}{You must specify the %
  copyright year}{Example: \protect\copyyear{2012}}\fi
\if|\SAP@authoremail|\ClassError{sapthesis}{You must specify the email of the author}{%
  Example: \protect\authoremail{author@email.com}}\fi

% Titlepage:

	\begin{titlepage}

    \newcommand{\SAP@parbox}[2][]{\hspace{1.72cm}\parbox[t][##1][t]{11.9cm}{##2}}
    \parindent=0pt
    \sffamily

    \vspace*{-10mm}
    \ifSAP@bn
      \includegraphics[width=5cm]{sapienza-MLblack-pos}
    \else
      \includegraphics[width=5cm]{sapienza-MLred-pos}
    \fi

    \nointerlineskip
    \vspace{\stretch{0.9}}
    \nointerlineskip

    \SAP@parbox{\LARGE\textcolor{sapred}{\@title}\par}

    \vspace{\stretch{0.9}}

    \SAP@parbox[3cm]{%
    \textcolor{sapred}{\SAP@courseorganizerLabel\\[2mm]
    Corso di Laurea \ifSAP@LaM Magistrale \fi in \SAP@courseLabel}}

    \SAP@parbox[3cm]{%
    \SAP@CandidateLabel\\[2mm]
    \@author\\[0.75mm]
    \SAP@IDnumberLabel\ \SAP@IDnumber}

    \SAP@parbox[4.5cm]{%
    \makebox[11.9cm][l]{%
      \parbox[t]{5.9cm}{%
        \ifcase\SAP@advisorcount
          \ClassError{sapthesis}{You must specify at least one advisor in the preamble}%
          {E.g. \protect\advisor{Prof. Name Surname}}
        \or
          \SAP@ThesisAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@advisortoks}
        \hspace{1.5cm}
        \parbox[t]{5.9cm}{%
        \ifcase\SAP@coadvisorcount
          \relax
        \or
          \SAP@ThesisCoAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisCoAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@coadvisortoks}
    }}

    \SAP@parbox{\SAP@AcademicYearLabel\ \SAP@submitdate}
    
	\end{titlepage}
\clearpage
\SAP@composebacktitlepage
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Master di primo livello and
%% Master di secondo livello maketitle

\newcommand{\SAP@maketitleMaster}{

% Errors:

\if|\SAP@courseorganizerLabel|\ClassError{sapthesis}{You must specify the degree %
  organizer}{Example: \protect\courseorganizer{Faculty of ...}}\fi
% \if|\SAP@submitdate|\ClassError{sapthesis}{You must specify the Academic %
%  Year}{Example: \protect\submitdate{2011/2012}}\fi
% \if|\SAP@IDnumber|\ClassError{sapthesis}{You must specify your %
%  ID number (matricola)}{E.g. 123456}\fi
\if|\SAP@copyyear|\ClassError{sapthesis}{You must specify the %
  copyright year}{Example: \protect\copyyear{2012}}\fi
\if|\SAP@authoremail|\ClassError{sapthesis}{You must specify the email of the author}{%
  Example: \protect\authoremail{author@email.com}}\fi

% Titlepage:

	\begin{titlepage}

    \newcommand{\SAP@parbox}[2][]{\hspace{1.72cm}\parbox[t][##1][t]{11.9cm}{##2}}
    \parindent=0pt
    \sffamily

    \vspace*{-10mm}
    \ifSAP@bn
      \includegraphics[width=5cm]{sapienza-MLblack-pos}
    \else
      \includegraphics[width=5cm]{sapienza-MLred-pos}
    \fi

    \nointerlineskip
    \vspace{\stretch{0.9}}
    \nointerlineskip

    \SAP@parbox{\LARGE\textcolor{sapred}{\@title}\par}

    \vspace{\stretch{0.9}}

    \SAP@parbox[3cm]{%
    \textcolor{sapred}{\SAP@courseorganizerLabel\\[2mm]
    Master di \ifSAP@MasterP primo \fi \ifSAP@MasterS secondo \fi livello in \SAP@courseLabel}}

    \SAP@parbox[3cm]{%
    \SAP@CandidateLabel\\[2mm]
    \@author\\[0.75mm]
    \SAP@IDnumberLabel\ \SAP@IDnumber}

    \SAP@parbox[4.5cm]{%
    \makebox[11.9cm][l]{%
      \parbox[t]{5.9cm}{%
        \ifcase\SAP@advisorcount
          \ClassError{sapthesis}{You must specify at least one advisor in the preamble}%
          {E.g. \protect\advisor{Prof. Name Surname}}
        \or
          \SAP@ThesisAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@advisortoks}
        \hspace{1.5cm}
        \parbox[t]{5.9cm}{%
        \ifcase\SAP@coadvisorcount
          \relax
        \or
          \SAP@ThesisCoAdvisorLabel\\[2mm]
        \else
          \SAP@ThesisCoAdvisorsLabel\\[2mm]
        \fi
        \the\SAP@coadvisortoks}
    }}

    \SAP@parbox{\SAP@AcademicYearLabel\ \SAP@submitdate}
    
	\end{titlepage}
\clearpage
\SAP@composebacktitlepage
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Macro for the back of the title page %%%%%%%%%%%%%%%%%%%%%%%%%%%%%
\def\SAP@composebacktitlepage{%

{\thispagestyle{empty}
\setlength{\parindent}{0pt}

%\ifSAP@noexaminfo
%\null
\hrulefill
\vfill
%\else
%\vspace*{\stretch{0.1}}
%
%\if?\SAP@examdate?
%  \SAP@ThesisNotDefensedLabel%
%\else
%  \SAP@ThesisDefensedLabelA\ \SAP@examdate\\
%  \SAP@ThesisDefensedLabelB:\\[2mm]
%  \the\SAP@examinertoks%
%\fi
%
\vspace{\stretch{0.8}}
%\fi

\hrulefill
\vspace{2mm}

\small
\textbf{\def\\{}\def\newline{}\@title} \\[0.75mm]
Technical Report\  
%\ifSAP@PhD\SAP@PhDthesistype\fi
%\ifSAP@LaM\SAP@LaMthesistype\fi
%\ifSAP@Lau\SAP@Lauthesistype\fi
%\ifSAP@MasterS\SAP@MasterSthesistype\fi
%\ifSAP@MasterP\SAP@MasterPthesistype\fi
\SAP@unisaname \\[0.75mm]
\if|\SAP@ISBN|\relax\else ISBN: \SAP@ISBN\fi\\[0.75mm]
\copyright\ \SAP@copyyear\ \@author. \SAP@allRightsReservedLabel\\[1.5mm]

%Material for which the author is the copyright owner cannot be
%used without the written permission of the author.
%The permission to reproduce copyright protected material does not extend 
%to any material that is copyright of a third party; authorization to
%reproduce such material must be obtained from the copyright owners concerned.
%If you believe that a copyright infringement is present in this thesis,
%please contact the author, providing your name, contact details, full 
%details of the copyright infringement and advise whether you are the copyright
%owner or a representative of the copyright owner.\\

\SAP@sapthesisInformationLabel.\\

\if|\SAP@versiondate|
  \relax%
\else
  \SAP@versionLabel\SAP@IDnumber\ -- \SAP@versiondate \\[2mm]%
\fi
\if|\SAP@website|
  \relax%
\else
  \SAP@websiteLabel: \@ifundefined{href}{\SAP@website}{\href{\SAP@website}{\SAP@website}} \\[2mm]%
\fi
\SAP@emailLabel: \@ifundefined{href}{\SAP@authoremail}{\href{mailto:\SAP@authoremail}{\SAP@authoremail}}\par

}
\cleardoublepage
}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% Choosing the right \maketitle according to the thesis type %%%%%%%

\renewcommand{\maketitle}{%
  \ifSAP@PhD\SAP@maketitlePhD\fi
  \ifSAP@LaM\SAP@maketitleLaurea\fi
  \ifSAP@Lau\SAP@maketitleLaurea\fi
  \ifSAP@MasterS\SAP@maketitleMaster\fi
  \ifSAP@MasterP\SAP@maketitleMaster\fi}



%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% DEDICATION COMMAND %%
\providecommand*{\dedication}[1]{%
\cleardoublepage
\thispagestyle{empty}
\null\vspace{\stretch{0.26}}
\begin{flushright}
\textit{#1}
\end{flushright}
\vspace{\stretch{0.74}}\null}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% ABSTRACT ENVIRONMENT %%
\newenvironment{abstract}[1][\SAP@AbstractLabel]
{\cleardoublepage\thispagestyle{plain}
\noindent{\Large\bfseries #1}\par\vspace{0.5cm}\noindent\ignorespaces}
{\null\ignorespacesafterend}

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% ACKNOWLEDGMENTS ENVIRONMENT %%
\newenvironment{acknowledgments}[1][\SAP@AcknowledgmentsLabel]
{\cleardoublepage\thispagestyle{plain}
\noindent{\Large\bfseries #1}\par\vspace{0.5cm}\itshape\noindent\ignorespaces}
{\null\ignorespacesafterend}


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% LANGUAGE COMMANDS %%

\newcommand{\SAP@english}{%
  \newcommand{\SAP@CandidateLabel}{Candidate}
  \newcommand{\SAP@IDnumberLabel}{ID number}
  \newcommand{\SAP@ThesisAdvisorLabel}{Author}
  \newcommand{\SAP@ThesisAdvisorsLabel}{Authors}
  \newcommand{\SAP@ThesisCoAdvisorLabel}{Co-Advisor}
  \newcommand{\SAP@ThesisCoAdvisorsLabel}{Co-Advisors}
  \newcommand{\SAP@DeclarationLabel}{A thesis
  submitted in partial fulfillment of the requirements\\
    for the degree of Doctor of Philosophy in}
  \newcommand{\SAP@ThesisNotDefensedLabel}{Thesis not yet defended}
  \newcommand{\SAP@ThesisDefensedLabelA}{Thesis defended on}
  \newcommand{\SAP@ThesisDefensedLabelB}{in front of a Board of Examiners composed by}
  \newcommand{\SAP@chairman}{chairman}
  \newcommand{\SAP@PhDthesistype}{Ph.D. thesis}
  \newcommand{\SAP@LaMthesistype}{Master thesis}
  \newcommand{\SAP@Lauthesistype}{Bachelor thesis}
  \newcommand{\SAP@MasterPthesistype}{First level master thesis}
  \newcommand{\SAP@MasterSthesistype}{Second level master thesis}
  \newcommand{\SAP@unisaname}{University of Salerno}
  \newcommand{\SAP@versionLabel}{Version Number:}
  \newcommand{\SAP@websiteLabel}{Website}
  \newcommand{\SAP@AcknowledgmentsLabel}{Acknowledgments}
  \newcommand{\SAP@AbstractLabel}{Abstract}
  \newcommand{\SAP@AcademicYearLabel}{Academic Year}
  \newcommand{\SAP@allRightsReservedLabel}{All rights reserved}
  \newcommand{\SAP@sapthesisInformationLabel}{This document has been typeset by \LaTeX\ and the DIThesis class}
  \newcommand{\SAP@emailLabel}{Author's email}}

\newcommand{\SAP@italian}{%
  \newcommand{\SAP@CandidateLabel}{Candidato}
  \newcommand{\SAP@IDnumberLabel}{Matricola}
  \newcommand{\SAP@ThesisAdvisorLabel}{Relatore}
  \newcommand{\SAP@ThesisAdvisorsLabel}{Relatori}
  \newcommand{\SAP@ThesisCoAdvisorLabel}{Correlatore}
  \newcommand{\SAP@ThesisCoAdvisorsLabel}{Correlatori}
  \newcommand{\SAP@DeclarationLabel}{Tesi presentata
  in parziale adempimento dei requisiti necessari\\
  per conseguire il titolo di Dottore di Ricerca in}
  \newcommand{\SAP@ThesisNotDefensedLabel}{Tesi non ancora discussa}
  \newcommand{\SAP@ThesisDefensedLabelA}{Tesi discussa il}
  \newcommand{\SAP@ThesisDefensedLabelB}{di fronte a una commissione esaminatrice composta da}
  \newcommand{\SAP@chairman}{presidente}
  \newcommand{\SAP@PhDthesistype}{Tesi di dottorato}
  \newcommand{\SAP@LaMthesistype}{Tesi di Laurea Magistrale}
  \newcommand{\SAP@Lauthesistype}{Tesi di Laurea}
  \newcommand{\SAP@MasterPthesistype}{Tesi di Master di primo livello}
  \newcommand{\SAP@MasterSthesistype}{Tesi di Master di secondo livello}
  \newcommand{\SAP@sapienzaname}{Sapienza -- Universit\`a di Roma}
  \newcommand{\SAP@versionLabel}{Versione}
  \newcommand{\SAP@websiteLabel}{Sito web}
  \newcommand{\SAP@AcknowledgmentsLabel}{Ringraziamenti}
  \newcommand{\SAP@AbstractLabel}{Sommario}
  \newcommand{\SAP@AcademicYearLabel}{Anno Accademico}
  \newcommand{\SAP@allRightsReservedLabel}{Tutti i diritti riservati}
  \newcommand{\SAP@sapthesisInformationLabel}{Questa tesi \`e stata composta con \LaTeX\ e la classe Sapthesis}
  \newcommand{\SAP@emailLabel}{Email dell'autore}}

  
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%% USEFUL COMMANDS %%

\newcommand*{\SAP@upbox}[1]{\textrm{\upshape#1}} 
\AtBeginDocument{

  \definecolor{sapred}{rgb}{0.5098039,0.1411765,0.2}

  % scritte in funzione della lingua del documento. Default inglese.
  \ifSAP@customlanguage
    \SAP@customlanguage
  \else
    \@ifpackageloaded{babel}{\csname SAP@\languagename\endcsname}{%
      \@ifpackageloaded{polyglossia}{\csname SAP@\languagename\endcsname}{\SAP@english}}
  \fi

  % degree 
  \providecommand{\degree}{\SAP@upbox{\textdegree}} 
  \providecommand{\g}{\degree} 

  % gradi Celsius
  \providecommand{\celsius}{\ensuremath{\SAP@upbox{\textdegree}\kern-\scriptspace\mathrm{C}}}
  \ifxetex
    \ProvideTextCommandDefault{\C}{\celsius}
  \else
    \providecommand{\C}{\celsius}
  \fi
  
  % Angstrom 
  \providecommand{\angstrom}{\SAP@upbox{\AA}} 
  \providecommand{\A}{\angstrom} 
  
  % micro 
  \providecommand{\micro}{\SAP@upbox{\textmu}} 
  
  % Ohm 
  \providecommand{\ohm}{\SAP@upbox{\textohm}} 
  
  % times (only math) 
  \providecommand{\x}{\times} 
  
  % measurement unit 
  \providecommand*{\un}[1]{\ensuremath{\mathrm{\,#1}}} 

  % Napier's number 
  \providecommand{\eu}{\ensuremath{\mathrm{e}}} 
  
  % imaginary unit 
  \providecommand{\iu}{\ensuremath{\mathrm{i}}} 
  
  % roman subscript 
  \providecommand*{\rb}[1]{\ensuremath{_\mathrm{#1}}} 
  
  % roman superscript 
  \providecommand*{\rp}[1]{\ensuremath{^\mathrm{#1}}} 

  % differential (only math)
  \ifSAP@romandiff
    \providecommand{\di}{\mathop{}\!\mathrm{d}}
  \else
    \providecommand{\di}{\mathop{}\!d}
  \fi
  
  % derivative and partial derivative (only math) 
  \providecommand*{\der}[3][]{\frac{d\if?#1?\else^{#1}\fi#2}{d #3\if?#1?\else^{#1}\fi}} 
  \providecommand*{\pder}[3][]{% 
    \frac{\partial\if?#1?\else^{#1}\fi#2}{\partial #3\if?#1?\else^{#1}\fi}% 
  }

  % subscript and superscript in text mode
  \DeclareRobustCommand*\textsubscript[1]{\@textsubscript{\selectfont#1}} 
  \def\@textsubscript#1{{\m@th\ensuremath{_{\mbox{\fontsize\sf@size\z@#1}}}}}
  \providecommand*{\tb}[1]{\textsubscript{#1}}
  \providecommand*{\tp}[1]{\textsuperscript{#1}}
}