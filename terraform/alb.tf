resource "aws_lb" "buildnest" {
  name               = "buildnest-alb"
  load_balancer_type = "application"
  subnets            = [aws_subnet.public.id, aws_subnet.public_2.id]
  security_groups    = [aws_security_group.alb_sg.id]

  tags = {
    Name = "buildnest-alb"
  }
}

resource "aws_lb_target_group" "buildnest_app" {
  name        = "buildnest-app-tg"
  port        = 8080
  protocol    = "HTTP"
  vpc_id      = aws_vpc.buildnest.id
  target_type = "ip"

  health_check {
    path                = "/actuator/health/readiness"
    healthy_threshold   = 2
    unhealthy_threshold = 3
    timeout             = 5
    interval            = 30
    matcher             = "200"
  }

  tags = {
    Name = "buildnest-app-tg"
  }
}

resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.buildnest.arn
  port              = 80
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.buildnest_app.arn
  }
}
