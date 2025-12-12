import nodemailer from 'nodemailer';

export const sendEmail = async (to, subject, text) => {
  // This is a stub: configure a real transporter in production
  console.log(`sendEmail -> to:${to} subject:${subject}`);
};
